package app.revanced.integrations.returnyoutubedislike;

import android.content.Context;
import android.icu.text.CompactDecimalFormat;
import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import android.os.Build;
import android.text.SpannableString;

import androidx.annotation.GuardedBy;
import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import app.revanced.integrations.returnyoutubedislike.requests.RYDVoteData;
import app.revanced.integrations.returnyoutubedislike.requests.ReturnYouTubeDislikeApi;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public class ReturnYouTubeDislike {
    /**
     * Maximum amount of time to block the UI from updates while waiting for dislike network call to complete.
     *
     * Must be less than 5 seconds, as per:
     * https://developer.android.com/topic/performance/vitals/anr
     */
    private static final long MILLISECONDS_TO_BLOCK_UI_WHILE_WAITING_FOR_DISLIKE_FETCH_TO_COMPLETE = 4000;

    /**
     * Used to send votes, one by one, in the same order the user created them
     */
    private static final ExecutorService voteSerialExecutor = Executors.newSingleThreadExecutor();

    // Must be volatile, since this is read/write from different threads
    private static volatile boolean isEnabled = SettingsEnum.RYD_ENABLED.getBoolean();

    /**
     * Used to guard {@link #currentVideoId} and {@link #voteFetchFuture},
     * as multiple threads access this class.
     */
    private static final Object videoIdLockObject = new Object();

    @GuardedBy("videoIdLockObject")
    private static String currentVideoId;

    /**
     * Stores the results of the vote api fetch, and used as a barrier to wait until fetch completes
     */
    @GuardedBy("videoIdLockObject")
    private static Future<RYDVoteData> voteFetchFuture;

    public enum Vote {
        LIKE(1),
        DISLIKE(-1),
        LIKE_REMOVE(0);

        public final int value;

        Vote(int value) {
            this.value = value;
        }
    }

    private ReturnYouTubeDislike() {
    } // only static methods

    /**
     * Used to format like/dislike count.
     */
    @GuardedBy("ReturnYouTubeDislike.class") // not thread safe
    private static CompactDecimalFormat dislikeCountFormatter;

    /**
     * Used to format like/dislike count.
     */
    @GuardedBy("ReturnYouTubeDislike.class") // not thread safe
    private static DecimalFormat dislikePercentageFormatter;

    public static void onEnabledChange(boolean enabled) {
        isEnabled = enabled;
    }

    private static String getCurrentVideoId() {
        synchronized (videoIdLockObject) {
            return currentVideoId;
        }
    }

    private static Future<RYDVoteData> getVoteFetchFuture() {
        synchronized (videoIdLockObject) {
            return voteFetchFuture;
        }
    }

    // It is unclear if this method is always called on the main thread (since the YouTube app is the one making the call)
    // treat this as if any thread could call this method
    public static void newVideoLoaded(String videoId) {
        if (!isEnabled) return;
        try {
            Objects.requireNonNull(videoId);
            LogHelper.printDebug(() -> "New video loaded: " + videoId);

            synchronized (videoIdLockObject) {
                currentVideoId = videoId;
                // no need to wrap the fetchDislike call in a try/catch,
                // as any exceptions are propagated out in the later Future#Get call
                voteFetchFuture = ReVancedUtils.submitOnBackgroundThread(() -> ReturnYouTubeDislikeApi.fetchVotes(videoId));
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to load new video: " + videoId, ex);
        }
    }

    // BEWARE! This method is sometimes called on the main thread, but it usually is called _off_ the main thread!
    public static void onComponentCreated(Object conversionContext, AtomicReference<Object> textRef) {
        if (!isEnabled) return;

        try {
            var conversionContextString = conversionContext.toString();

            boolean isSegmentedButton = false;
            // Check for new component
            if (conversionContextString.contains("|segmented_like_dislike_button.eml|")) {
                isSegmentedButton = true;
            } else if (!conversionContextString.contains("|dislike_button.eml|")) {
                return;
            }

            // Have to block the current thread until fetching is done
            // There's no known way to edit the text after creation yet
            RYDVoteData votingData;
            long fetchStartTime = 0;
            try {
                Future<RYDVoteData> fetchFuture = getVoteFetchFuture();
                if (SettingsEnum.DEBUG.getBoolean() && !fetchFuture.isDone()) {
                    fetchStartTime = System.currentTimeMillis();
                }
                votingData = fetchFuture.get(MILLISECONDS_TO_BLOCK_UI_WHILE_WAITING_FOR_DISLIKE_FETCH_TO_COMPLETE, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                LogHelper.printDebug(() -> "UI timed out waiting for dislike fetch to complete");
                return;
            } finally {
                recordTimeUISpentWaitingForNetworkCall(fetchStartTime);
            }
            if (votingData == null) {
                LogHelper.printDebug(() -> "Cannot add dislike count to UI (RYD data not available)");
                return;
            }

            updateDislike(textRef, isSegmentedButton, votingData);
            LogHelper.printDebug(() -> "Updated text");
        } catch (Exception ex) {
            LogHelper.printException(() -> "Error while trying to update dislikes text", ex);
        }
    }

    public static void sendVote(Vote vote) {
        if (!isEnabled) return;
        try {
            Objects.requireNonNull(vote);
            Context context = Objects.requireNonNull(ReVancedUtils.getContext());
            if (SharedPrefHelper.getBoolean(context, SharedPrefHelper.SharedPrefNames.YOUTUBE, "user_signed_out", true)) {
                return;
            }

            // Must make a local copy of videoId, since it may change between now and when the vote thread runs
            String videoIdToVoteFor = getCurrentVideoId();

            voteSerialExecutor.execute(() -> {
                // must wrap in try/catch to properly log exceptions
                try {
                    String userId = getUserId();
                    if (userId != null) {
                        ReturnYouTubeDislikeApi.sendVote(videoIdToVoteFor, userId, vote);
                    }
                } catch (Exception ex) {
                    LogHelper.printException(() -> "Failed to send vote", ex);
                }
            });
        } catch (Exception ex) {
            LogHelper.printException(() -> "Error while trying to send vote", ex);
        }
    }

    /**
     * Must call off main thread, as this will make a network call if user has not yet been registered
     *
     * @return ReturnYouTubeDislike user ID. If user registration has never happened
     * and the network call fails, this will return NULL
     */
    @Nullable
    private static String getUserId() {
        ReVancedUtils.verifyOffMainThread();

        String userId = SettingsEnum.RYD_USER_ID.getString();
        if (userId != null) {
            return userId;
        }

        userId = ReturnYouTubeDislikeApi.registerAsNewUser(); // blocks until network call is completed
        if (userId != null) {
            SettingsEnum.RYD_USER_ID.saveValue(userId);
        }
        return userId;
    }

    private static void updateDislike(AtomicReference<Object> textRef, boolean isSegmentedButton, RYDVoteData voteData) {
        SpannableString oldSpannableString = (SpannableString) textRef.get();
        String newDislikeString = SettingsEnum.RYD_SHOW_DISLIKE_PERCENTAGE.getBoolean()
                ? formatDislikePercentage(voteData.dislikePercentage)
                : formatDislikeCount(voteData.dislikeCount);

        if (isSegmentedButton) { // both likes and dislikes are on a custom segmented button
            // parse out the like count as a string
            String oldLikesString = oldSpannableString.toString().split(" \\| ")[0];

            // YouTube creators can hide the like count on a video,
            // and the like count appears as a device language specific string that says 'Like'
            // check if the first character is not a number
            if (!Character.isDigit(oldLikesString.charAt(0))) {
                // likes are hidden.
                // RYD does not provide usable data for these types of videos,
                // and the API returns bogus data (zero likes and zero dislikes)
                //
                // example video: https://www.youtube.com/watch?v=UnrU5vxCHxw
                // RYD data: https://returnyoutubedislikeapi.com/votes?videoId=UnrU5vxCHxw
                //
                // discussion about this: https://github.com/Anarios/return-youtube-dislike/discussions/530

                //
                // Change the "Likes" string to show that likes and dislikes are hidden
                //
                newDislikeString = "Hidden"; // for now, this is not localized
                LogHelper.printDebug(() -> "Like count is hidden by video creator. "
                        + "RYD does not provide data for videos with hidden likes.");
            } else {
                // temporary fix for https://github.com/revanced/revanced-integrations/issues/118
                newDislikeString = oldLikesString + " | " + newDislikeString;
            }
        }

        SpannableString newSpannableString = new SpannableString(newDislikeString);
        // Copy style (foreground color, etc) to new string
        Object[] spans = oldSpannableString.getSpans(0, oldSpannableString.length(), Object.class);
        for (Object span : spans) {
            newSpannableString.setSpan(span, 0, newDislikeString.length(), oldSpannableString.getSpanFlags(span));
        }
        textRef.set(newSpannableString);
    }

    private static String formatDislikeCount(long dislikeCount) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String formatted;
            synchronized (ReturnYouTubeDislike.class) { // number formatter is not thread safe, must synchronize
                if (dislikeCountFormatter == null) {
                    Locale locale = ReVancedUtils.getContext().getResources().getConfiguration().locale;
                    LogHelper.printDebug(() -> "Locale: " + locale);
                    dislikeCountFormatter = CompactDecimalFormat.getInstance(locale, CompactDecimalFormat.CompactStyle.SHORT);
                }
                formatted = dislikeCountFormatter.format(dislikeCount);
            }
            LogHelper.printDebug(() -> "Dislike count: " + dislikeCount + " formatted as: " + formatted);
            return formatted;
        }

        // never will be reached, as the oldest supported YouTube app requires Android N or greater
        return String.valueOf(dislikeCount);
    }

    private static String formatDislikePercentage(float dislikePercentage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String formatted;
            synchronized (ReturnYouTubeDislike.class) { // number formatter is not thread safe, must synchronize
                if (dislikePercentageFormatter == null) {
                    Locale locale = ReVancedUtils.getContext().getResources().getConfiguration().locale;
                    LogHelper.printDebug(() -> "Locale: " + locale);
                    dislikePercentageFormatter = new DecimalFormat("", new DecimalFormatSymbols(locale));
                }
                if (dislikePercentage == 0 || dislikePercentage >= 0.01) { // zero, or at least 1%
                    dislikePercentageFormatter.applyLocalizedPattern("0"); // show only whole percentage points
                } else { // between (0, 1)%
                    dislikePercentageFormatter.applyLocalizedPattern("0.#"); // show 1 digit precision
                }
                final char percentChar = dislikePercentageFormatter.getDecimalFormatSymbols().getPercent();
                formatted = dislikePercentageFormatter.format(100 * dislikePercentage) + percentChar;
            }
            LogHelper.printDebug(() -> "Dislike percentage: " + dislikePercentage + " formatted as: " + formatted);
            return formatted;
        }

        // never will be reached, as the oldest supported YouTube app requires Android N or greater
        return (int) (100 * dislikePercentage) + "%";
    }


    /**
     * Number of times the UI was forced to wait on a network fetch to complete
     */
    private static volatile int numberOfTimesUIWaitedOnNetworkCalls;

    /**
     * Total time the UI waited, of all times it was forced to wait.
     */
    private static volatile long totalTimeUIWaitedOnNetworkCalls;

    private static void recordTimeUISpentWaitingForNetworkCall(long timeUIWaitStarted) {
        if (timeUIWaitStarted == 0 || !SettingsEnum.DEBUG.getBoolean()) {
            return;
        }
        final long timeUIWaitingTotal = System.currentTimeMillis() - timeUIWaitStarted;
        LogHelper.printDebug(() -> "UI thread waited for: " + timeUIWaitingTotal + "ms for vote fetch to complete");

        totalTimeUIWaitedOnNetworkCalls += timeUIWaitingTotal;
        numberOfTimesUIWaitedOnNetworkCalls++;
        final long averageTimeForcedToWait = totalTimeUIWaitedOnNetworkCalls / numberOfTimesUIWaitedOnNetworkCalls;
        LogHelper.printDebug(() -> "UI thread forced to wait: " + numberOfTimesUIWaitedOnNetworkCalls + " times, "
                + "total wait time: " + totalTimeUIWaitedOnNetworkCalls + "ms, "
                + "average wait time: " + averageTimeForcedToWait + "ms") ;
    }
}
