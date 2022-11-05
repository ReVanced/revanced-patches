package app.revanced.integrations.returnyoutubedislike;

import android.content.Context;
import android.icu.text.CompactDecimalFormat;
import android.os.Build;
import android.text.SpannableString;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import app.revanced.integrations.returnyoutubedislike.requests.ReturnYouTubeDislikeApi;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public class ReturnYouTubeDislike {
    private static String currentVideoId;
    public static Integer dislikeCount;

    private static boolean isEnabled;
    private static boolean segmentedButton;

    public enum Vote {
        LIKE(1),
        DISLIKE(-1),
        LIKE_REMOVE(0);

        public int value;

        Vote(int value) {
            this.value = value;
        }
    }

    private static Thread _dislikeFetchThread = null;
    private static Thread _votingThread = null;
    private static Registration registration;
    private static Voting voting;
    private static CompactDecimalFormat compactNumberFormatter;

    static {
        Context context = ReVancedUtils.getContext();
        isEnabled = SettingsEnum.RYD_ENABLED.getBoolean();
        if (isEnabled) {
            registration = new Registration();
            voting = new Voting(registration);
        }

        Locale locale = context.getResources().getConfiguration().locale;
        LogHelper.debug(ReturnYouTubeDislike.class, "locale - " + locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            compactNumberFormatter = CompactDecimalFormat.getInstance(
                    locale,
                    CompactDecimalFormat.CompactStyle.SHORT
            );
        }
    }

    public static void onEnabledChange(boolean enabled) {
        isEnabled = enabled;
        if (registration == null) {
            registration = new Registration();
        }
        if (voting == null) {
            voting = new Voting(registration);
        }
    }

    public static void newVideoLoaded(String videoId) {
        LogHelper.debug(ReturnYouTubeDislike.class, "newVideoLoaded - " + videoId);

        dislikeCount = null;
        if (!isEnabled) return;

        currentVideoId = videoId;

        try {
            if (_dislikeFetchThread != null && _dislikeFetchThread.getState() != Thread.State.TERMINATED) {
                LogHelper.debug(ReturnYouTubeDislike.class, "Interrupting the thread. Current state " + _dislikeFetchThread.getState());
                _dislikeFetchThread.interrupt();
            }
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislike.class, "Error in the dislike fetch thread", ex);
        }

        _dislikeFetchThread = new Thread(() -> ReturnYouTubeDislikeApi.fetchDislikes(videoId));
        _dislikeFetchThread.start();
    }

    public static void onComponentCreated(Object conversionContext, AtomicReference<Object> textRef) {
        if (!isEnabled) return;

        try {
            var conversionContextString = conversionContext.toString();

            // Check for new component
            if (conversionContextString.contains("|segmented_like_dislike_button.eml|"))
                segmentedButton = true;
            else if (!conversionContextString.contains("|dislike_button.eml|"))
                return;


            // Have to block the current thread until fetching is done
            // There's no known way to edit the text after creation yet
            if (_dislikeFetchThread != null) _dislikeFetchThread.join();

            if (dislikeCount == null) return;

            updateDislike(textRef, dislikeCount);
            LogHelper.debug(ReturnYouTubeDislike.class, "Updated text on component" + conversionContextString);
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislike.class, "Error while trying to set dislikes text", ex);
        }
    }

    public static void sendVote(Vote vote) {
        if (!isEnabled) return;

        Context context = ReVancedUtils.getContext();
        if (SharedPrefHelper.getBoolean(Objects.requireNonNull(context), SharedPrefHelper.SharedPrefNames.YOUTUBE, "user_signed_out", true))
            return;

        LogHelper.debug(ReturnYouTubeDislike.class, "sending vote - " + vote + " for video " + currentVideoId);
        try {
            if (_votingThread != null && _votingThread.getState() != Thread.State.TERMINATED) {
                LogHelper.debug(ReturnYouTubeDislike.class, "Interrupting the thread. Current state " + _votingThread.getState());
                _votingThread.interrupt();
            }
        } catch (Exception ex) {
            LogHelper.printException(ReturnYouTubeDislike.class, "Error in the voting thread", ex);
        }

        _votingThread = new Thread(() -> {
            try {
                boolean result = voting.sendVote(currentVideoId, vote);
                LogHelper.debug(ReturnYouTubeDislike.class, "sendVote status " + result);
            } catch (Exception ex) {
                LogHelper.printException(ReturnYouTubeDislike.class, "Failed to send vote", ex);
            }
        });
        _votingThread.start();
    }

    private static void updateDislike(AtomicReference<Object> textRef, Integer dislikeCount) {
        SpannableString oldSpannableString = (SpannableString) textRef.get();

        // parse the buttons string
        // if the button is segmented, only get the like count as a string
        var oldButtonString = oldSpannableString.toString();
        if (segmentedButton) oldButtonString = oldButtonString.split(" \\| ")[0];

        var dislikeString = formatDislikes(dislikeCount);
        SpannableString newString = new SpannableString(
                segmentedButton ? (oldButtonString + " | " + dislikeString) : dislikeString
        );

        // Copy style (foreground color, etc) to new string
        Object[] spans = oldSpannableString.getSpans(0, oldSpannableString.length(), Object.class);
        for (Object span : spans)
            newString.setSpan(span, 0, newString.length(), oldSpannableString.getSpanFlags(span));

        textRef.set(newString);
    }

    private static String formatDislikes(int dislikes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && compactNumberFormatter != null) {
            final String formatted = compactNumberFormatter.format(dislikes);
            LogHelper.debug(ReturnYouTubeDislike.class, "Formatting dislikes - " + dislikes + " - " + formatted);
            return formatted;
        }
        LogHelper.debug(ReturnYouTubeDislike.class, "Couldn't format dislikes, using the unformatted count - " + dislikes);
        return String.valueOf(dislikes);
    }
}
