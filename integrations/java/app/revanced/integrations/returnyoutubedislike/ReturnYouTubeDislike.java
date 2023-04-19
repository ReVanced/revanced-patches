package app.revanced.integrations.returnyoutubedislike;

import static app.revanced.integrations.utils.StringRef.str;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.icu.text.CompactDecimalFormat;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import app.revanced.integrations.returnyoutubedislike.requests.RYDVoteData;
import app.revanced.integrations.returnyoutubedislike.requests.ReturnYouTubeDislikeApi;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.ThemeHelper;

/**
 * Because Litho creates spans using multiple threads, this entire class supports multithreading as well.
 */
public class ReturnYouTubeDislike {
    /**
     * Maximum amount of time to block the UI from updates while waiting for network call to complete.
     *
     * Must be less than 5 seconds, as per:
     * https://developer.android.com/topic/performance/vitals/anr
     */
    private static final long MAX_MILLISECONDS_TO_BLOCK_UI_WHILE_WAITING_FOR_FETCH_VOTES_TO_COMPLETE = 4000;

    /**
     * Unique placeholder character, used to detect if a segmented span already has dislikes added to it.
     * Can be any almost any non-visible character.
     */
    private static final char MIDDLE_SEPARATOR_CHARACTER = '\u2009'; // 'narrow space' character

    /**
     * Used to send votes, one by one, in the same order the user created them.
     */
    private static final ExecutorService voteSerialExecutor = Executors.newSingleThreadExecutor();

    /**
     * Used to guard {@link #currentVideoId} and {@link #voteFetchFuture}.
     */
    private static final Object videoIdLockObject = new Object();

    @Nullable
    @GuardedBy("videoIdLockObject")
    private static String currentVideoId;

    /**
     * If {@link #currentVideoId} and the RYD data is for the last shorts loaded.
     */
    private static volatile boolean lastVideoLoadedWasShort;

    /**
     * Stores the results of the vote api fetch, and used as a barrier to wait until fetch completes.
     */
    @Nullable
    @GuardedBy("videoIdLockObject")
    private static Future<RYDVoteData> voteFetchFuture;

    /**
     * Original dislike span, before modifications.
     */
    @Nullable
    @GuardedBy("videoIdLockObject")
    private static Spanned originalDislikeSpan;

    /**
     * Replacement like/dislike span that includes formatted dislikes.
     * Used to prevent recreating the same span multiple times.
     */
    @Nullable
    @GuardedBy("videoIdLockObject")
    private static SpannableString replacementLikeDislikeSpan;

    /**
     * For formatting dislikes as number.
     */
    @GuardedBy("ReturnYouTubeDislike.class") // not thread safe
    private static CompactDecimalFormat dislikeCountFormatter;

    /**
     * For formatting dislikes as percentage.
     */
    @GuardedBy("ReturnYouTubeDislike.class")
    private static NumberFormat dislikePercentageFormatter;

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

    public static void onEnabledChange(boolean enabled) {
        if (!enabled) {
            // Must clear old values, to protect against using stale data
            // if the user re-enables RYD while watching a video.
            setCurrentVideoId(null);
        }
    }

    private static void setCurrentVideoId(@Nullable String videoId) {
        synchronized (videoIdLockObject) {
            if (videoId == null && currentVideoId != null) {
                LogHelper.printDebug(() -> "Clearing data");
            }
            currentVideoId = videoId;
            lastVideoLoadedWasShort = false;
            voteFetchFuture = null;
            originalDislikeSpan = null;
            replacementLikeDislikeSpan = null;
        }
    }

    /**
     * Should be called after a user dislikes, or if the user changes settings for dislikes appearance.
     */
    public static void clearCache() {
        synchronized (videoIdLockObject) {
            if (replacementLikeDislikeSpan != null) {
                LogHelper.printDebug(() -> "Clearing cache");
            }
            replacementLikeDislikeSpan = null;
        }
    }

    @Nullable
    private static String getCurrentVideoId() {
        synchronized (videoIdLockObject) {
            return currentVideoId;
        }
    }

    @Nullable
    private static Future<RYDVoteData> getVoteFetchFuture() {
        synchronized (videoIdLockObject) {
            return voteFetchFuture;
        }
    }

    public static void newVideoLoaded(@NonNull String videoId) {
        Objects.requireNonNull(videoId);

        PlayerType currentPlayerType = PlayerType.getCurrent();
        if (currentPlayerType == PlayerType.INLINE_MINIMAL) {
            LogHelper.printDebug(() -> "Ignoring inline playback of video: " + videoId);
            setCurrentVideoId(null);
            return;
        }
        synchronized (videoIdLockObject) {
            if (videoId.equals(currentVideoId)) {
                return; // already loaded
            }
            if (!ReVancedUtils.isNetworkConnected()) { // must do network check after verifying it's a new video id
                LogHelper.printDebug(() -> "Network not connected, ignoring video: " + videoId);
                setCurrentVideoId(null);
                return;
            }
            LogHelper.printDebug(() -> "New video loaded: " + videoId + " playerType: " + currentPlayerType);
            setCurrentVideoId(videoId);

            // If a Short is opened while a regular video is on screen, this will incorrectly set this as false.
            // But this check is needed to fix unusual situations of opening/closing the app
            // while both a regular video and a short are on screen.
            lastVideoLoadedWasShort = PlayerType.getCurrent().isNoneOrHidden();

            // No need to wrap the call in a try/catch,
            // as any exceptions are propagated out in the later Future#Get call.
            voteFetchFuture = ReVancedUtils.submitOnBackgroundThread(() -> ReturnYouTubeDislikeApi.fetchVotes(videoId));
        }
    }

    /**
     * @return NULL if the span does not need changing or if RYD is not available.
     */
    @Nullable
    public static SpannableString getDislikeSpanForContext(@NonNull Object conversionContext, @NonNull CharSequence original) {
        if (PlayerType.getCurrent().isNoneOrHidden()) {
            return null;
        }
        String conversionContextString = conversionContext.toString();
        final boolean isSegmentedButton;
        if (conversionContextString.contains("|segmented_like_dislike_button.eml|")) {
            isSegmentedButton = true;
        } else if (conversionContextString.contains("|dislike_button.eml|")) {
            isSegmentedButton = false;
        } else {
            return null;
        }

        if (lastVideoLoadedWasShort) {
            // user:
            // 1, opened a video
            // 2. opened a short (without closing the regular video)
            // 3. closed the short
            // 4. regular video is now present, but the videoId and RYD data is still for the short
            LogHelper.printDebug(() -> "Ignoring getDislikeSpanForContext(), as data loaded is for prior short");
            return null;
        }

        return waitForFetchAndUpdateReplacementSpan((Spannable) original, isSegmentedButton);
    }

    /**
     * Called when a Shorts dislike Spannable is created.
     */
    public static SpannableString getDislikeSpanForShort(@NonNull Spanned original) {
        lastVideoLoadedWasShort = true; // it's now certain the video and data are a short
        return waitForFetchAndUpdateReplacementSpan(original, false);
    }

    // Alternatively, this could check if the span contains one of the custom created spans, but this is simple and quick.
    private static boolean isPreviouslyCreatedSegmentedSpan(@NonNull Spanned span) {
        return span.toString().indexOf(MIDDLE_SEPARATOR_CHARACTER) != -1;
    }

    /**
     * @return NULL if the span does not need changing or if RYD is not available.
     */
    @Nullable
    private static SpannableString waitForFetchAndUpdateReplacementSpan(@NonNull Spanned oldSpannable, boolean isSegmentedButton) {
        try {
            synchronized (videoIdLockObject) {
                if (replacementLikeDislikeSpan != null) {
                    String oldSpannableString = oldSpannable.toString();
                    if (replacementLikeDislikeSpan.toString().equals(oldSpannableString)) {
                        LogHelper.printDebug(() -> "Ignoring previously created dislikes span");
                        return null;
                    }
                    if (originalDislikeSpan.toString().equals(oldSpannableString)) {
                        LogHelper.printDebug(() -> "Replacing span with previously created dislike span");
                        return replacementLikeDislikeSpan;
                    }
                }
                if (isSegmentedButton && isPreviouslyCreatedSegmentedSpan(oldSpannable)) {
                    // need to recreate using original, as oldSpannable has prior outdated dislike values
                    oldSpannable = originalDislikeSpan;
                    if (oldSpannable == null) {
                        LogHelper.printDebug(() -> "Cannot add dislikes - original span is null"); // should never happen
                        return null;
                    }
                } else {
                    originalDislikeSpan = oldSpannable; // most up to date original
                }
            }

            // Must block the current thread until fetching is done.
            // There's no known way to edit the text after creation yet.
            Future<RYDVoteData> fetchFuture = getVoteFetchFuture();
            if (fetchFuture == null) {
                LogHelper.printDebug(() -> "fetch future not available (user enabled RYD while video was playing?)");
                return null;
            }
            RYDVoteData votingData = fetchFuture.get(MAX_MILLISECONDS_TO_BLOCK_UI_WHILE_WAITING_FOR_FETCH_VOTES_TO_COMPLETE, TimeUnit.MILLISECONDS);
            if (votingData == null) {
                LogHelper.printDebug(() -> "Cannot add dislike to UI (RYD data not available)");
                return null;
            }

            SpannableString replacement = createDislikeSpan(oldSpannable, isSegmentedButton, votingData);
            synchronized (videoIdLockObject) {
                replacementLikeDislikeSpan = replacement;
            }
            final Spanned oldSpannableLogging = oldSpannable;
            LogHelper.printDebug(() -> "Replaced: '" + oldSpannableLogging + "' with: '" + replacement + "'");
            return replacement;
        } catch (TimeoutException e) {
            LogHelper.printDebug(() -> "UI timed out while waiting for fetch votes to complete"); // show no toast
        } catch (Exception e) {
            LogHelper.printException(() -> "waitForFetchAndUpdateReplacementSpan failure", e); // should never happen
        }
        return null;
    }

    public static void sendVote(@NonNull Vote vote) {
        ReVancedUtils.verifyOnMainThread();
        Objects.requireNonNull(vote);
        try {
            // Must make a local copy of videoId, since it may change between now and when the vote thread runs.
            String videoIdToVoteFor = getCurrentVideoId();
            if (videoIdToVoteFor == null || lastVideoLoadedWasShort != PlayerType.getCurrent().isNoneOrHidden()) {
                // User enabled RYD after starting playback of a video.
                // Or shorts was loaded with regular video present, then shorts was closed,
                // and then user voted on the now visible original video.
                // Cannot send a vote, because the loaded videoId is for the wrong video.
                ReVancedUtils.showToastLong(str("revanced_ryd_failure_ryd_enabled_while_playing_video_then_user_voted"));
                return;
            }

            voteSerialExecutor.execute(() -> {
                try { // must wrap in try/catch to properly log exceptions
                    String userId = getUserId();
                    if (userId != null) {
                        ReturnYouTubeDislikeApi.sendVote(videoIdToVoteFor, userId, vote);
                    }
                } catch (Exception ex) {
                    LogHelper.printException(() -> "Failed to send vote", ex);
                }
            });

            clearCache(); // UI needs updating

            // Update the downloaded vote data.
            Future<RYDVoteData> future = getVoteFetchFuture();
            if (future == null) {
                LogHelper.printException(() -> "Cannot update UI dislike count - vote fetch is null");
                return;
            }
            // The future should always be completed before user can like/dislike, but use a timeout just in case.
            RYDVoteData voteData = future.get(MAX_MILLISECONDS_TO_BLOCK_UI_WHILE_WAITING_FOR_FETCH_VOTES_TO_COMPLETE, TimeUnit.MILLISECONDS);
            if (voteData == null) {
                // RYD fetch failed
                LogHelper.printDebug(() -> "Cannot update UI (vote data not available)");
                return;
            }
            voteData.updateUsingVote(vote);
        } catch (Exception ex) {
            LogHelper.printException(() -> "Error trying to send vote", ex);
        }
    }

    /**
     * Must call off main thread, as this will make a network call if user is not yet registered.
     *
     * @return ReturnYouTubeDislike user ID. If user registration has never happened
     * and the network call fails, this returns NULL.
     */
    @Nullable
    private static String getUserId() {
        ReVancedUtils.verifyOffMainThread();

        String userId = SettingsEnum.RYD_USER_ID.getString();
        if (!userId.isEmpty()) {
            return userId;
        }

        userId = ReturnYouTubeDislikeApi.registerAsNewUser();
        if (userId != null) {
            SettingsEnum.RYD_USER_ID.saveValue(userId);
        }
        return userId;
    }

    /**
     * @param isSegmentedButton If UI is using the segmented single UI component for both like and dislike.
     */
    private static SpannableString createDislikeSpan(@NonNull Spanned oldSpannable, boolean isSegmentedButton, @NonNull RYDVoteData voteData) {
        if (!isSegmentedButton) {
            // Simple replacement of 'dislike' with a number/percentage.
            return newSpannableWithDislikes(oldSpannable, voteData);
        }

        // Note: Some locales use right to left layout (arabic, hebrew, etc),
        // and care must be taken to retain the existing RTL encoding character on the likes string,
        // otherwise text will incorrectly show as left to right.
        // If making changes to this code, change device settings to a RTL language and verify layout is correct.
        String oldLikesString = oldSpannable.toString();

        // YouTube creators can hide the like count on a video,
        // and the like count appears as a device language specific string that says 'Like'.
        // Check if the string contains any numbers.
        if (!stringContainsNumber(oldLikesString)) {
            // Likes are hidden.
            // RYD does not provide usable data for these types of videos,
            // and the API returns bogus data (zero likes and zero dislikes)
            // discussion about this: https://github.com/Anarios/return-youtube-dislike/discussions/530
            //
            // example video: https://www.youtube.com/watch?v=UnrU5vxCHxw
            // RYD data: https://returnyoutubedislikeapi.com/votes?videoId=UnrU5vxCHxw
            //
            // Change the "Likes" string to show that likes and dislikes are hidden.
            String hiddenMessageString = str("revanced_ryd_video_likes_hidden_by_video_owner");
            return newSpanUsingStylingOfAnotherSpan(oldSpannable, hiddenMessageString);
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        final boolean compactLayout = SettingsEnum.RYD_USE_COMPACT_LAYOUT.getBoolean();
        final int separatorColor = ThemeHelper.isDarkTheme()
                ? 0x29AAAAAA  // transparent dark gray
                : 0xFFD9D9D9; // light gray
        DisplayMetrics dp = Objects.requireNonNull(ReVancedUtils.getContext()).getResources().getDisplayMetrics();

        if (!compactLayout) {
            // left separator
            final Rect leftSeparatorBounds = new Rect(0, 0,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.2f, dp),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, dp));
            String leftSeparatorString = ReVancedUtils.isRightToLeftTextLayout()
                    ? "\u200F    "  // u200F = right to left character
                    : "\u200E    "; // u200E = left to right character
            Spannable leftSeparatorSpan = new SpannableString(leftSeparatorString);
            ShapeDrawable shapeDrawable = new ShapeDrawable(new RectShape());
            shapeDrawable.getPaint().setColor(separatorColor);
            shapeDrawable.setBounds(leftSeparatorBounds);
            leftSeparatorSpan.setSpan(new VerticallyCenteredImageSpan(shapeDrawable), 1, 2,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE); // drawable cannot overwrite RTL or LTR character
            builder.append(leftSeparatorSpan);
        }

        // likes
        builder.append(newSpanUsingStylingOfAnotherSpan(oldSpannable, oldLikesString));

        // middle separator
        String middleSeparatorString = compactLayout
                ? "  " + MIDDLE_SEPARATOR_CHARACTER + "  "
                : "  \u2009" + MIDDLE_SEPARATOR_CHARACTER + "\u2009  "; // u2009 = 'narrow space' character
        final int shapeInsertionIndex = middleSeparatorString.length() / 2;
        final int middleSeparatorSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3.7f, dp);
        final Rect middleSeparatorBounds = new Rect(0, 0, middleSeparatorSize, middleSeparatorSize);
        Spannable middleSeparatorSpan = new SpannableString(middleSeparatorString);
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        shapeDrawable.getPaint().setColor(separatorColor);
        shapeDrawable.setBounds(middleSeparatorBounds);
        middleSeparatorSpan.setSpan(new VerticallyCenteredImageSpan(shapeDrawable), shapeInsertionIndex, shapeInsertionIndex + 1,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append(middleSeparatorSpan);

        // dislikes
        builder.append(newSpannableWithDislikes(oldSpannable, voteData));

        return new SpannableString(builder);
    }

    /**
     * Correctly handles any unicode numbers (such as Arabic numbers).
     *
     * @return if the string contains at least 1 number.
     */
    private static boolean stringContainsNumber(@NonNull String text) {
        for (int index = 0, length = text.length(); index < length; index++) {
            if (Character.isDigit(text.codePointAt(index))) {
                return true;
            }
        }
        return false;
    }

    private static SpannableString newSpannableWithDislikes(@NonNull Spanned sourceStyling, @NonNull RYDVoteData voteData) {
        return newSpanUsingStylingOfAnotherSpan(sourceStyling,
                SettingsEnum.RYD_SHOW_DISLIKE_PERCENTAGE.getBoolean()
                        ? formatDislikePercentage(voteData.getDislikePercentage())
                        : formatDislikeCount(voteData.getDislikeCount()));
    }

    private static SpannableString newSpanUsingStylingOfAnotherSpan(@NonNull Spanned sourceStyle, @NonNull String newSpanText) {
        SpannableString destination = new SpannableString(newSpanText);
        Object[] spans = sourceStyle.getSpans(0, sourceStyle.length(), Object.class);
        for (Object span : spans) {
            destination.setSpan(span, 0, destination.length(), sourceStyle.getSpanFlags(span));
        }
        return destination;
    }

    private static String formatDislikeCount(long dislikeCount) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            synchronized (ReturnYouTubeDislike.class) { // number formatter is not thread safe, must synchronize
                if (dislikeCountFormatter == null) {
                    // Note: Java number formatters will use the locale specific number characters.
                    // such as Arabic which formats "1.234" into "۱,۲۳٤"
                    // But YouTube disregards locale specific number characters
                    // and instead shows english number characters everywhere.
                    Locale locale = Objects.requireNonNull(ReVancedUtils.getContext()).getResources().getConfiguration().locale;
                    LogHelper.printDebug(() -> "Locale: " + locale);
                    dislikeCountFormatter = CompactDecimalFormat.getInstance(locale, CompactDecimalFormat.CompactStyle.SHORT);
                }
                return dislikeCountFormatter.format(dislikeCount);
            }
        }

        // will never be reached, as the oldest supported YouTube app requires Android N or greater
        return String.valueOf(dislikeCount);
    }

    private static String formatDislikePercentage(float dislikePercentage) {
        synchronized (ReturnYouTubeDislike.class) { // number formatter is not thread safe, must synchronize
            if (dislikePercentageFormatter == null) {
                Locale locale = Objects.requireNonNull(ReVancedUtils.getContext()).getResources().getConfiguration().locale;
                LogHelper.printDebug(() -> "Locale: " + locale);
                dislikePercentageFormatter = NumberFormat.getPercentInstance(locale);
            }
            if (dislikePercentage >= 0.01) { // at least 1%
                dislikePercentageFormatter.setMaximumFractionDigits(0); // show only whole percentage points
            } else {
                dislikePercentageFormatter.setMaximumFractionDigits(1); // show up to 1 digit precision
            }
            return dislikePercentageFormatter.format(dislikePercentage);
        }
    }
}

class VerticallyCenteredImageSpan extends ImageSpan {
    public VerticallyCenteredImageSpan(Drawable drawable) {
        super(drawable);
    }

    @Override
    public int getSize(@NonNull Paint paint, @NonNull CharSequence text,
                       int start, int end, @Nullable Paint.FontMetricsInt fontMetrics) {
        Drawable drawable = getDrawable();
        Rect bounds = drawable.getBounds();
        if (fontMetrics != null) {
            Paint.FontMetricsInt paintMetrics = paint.getFontMetricsInt();
            final int fontHeight = paintMetrics.descent - paintMetrics.ascent;
            final int drawHeight = bounds.bottom - bounds.top;
            final int yCenter = paintMetrics.ascent + fontHeight / 2;

            fontMetrics.ascent = yCenter - drawHeight / 2;
            fontMetrics.top = fontMetrics.ascent;
            fontMetrics.bottom = yCenter + drawHeight / 2;
            fontMetrics.descent = fontMetrics.bottom;
        }
        return bounds.right;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, @NonNull Paint paint) {
        Drawable drawable = getDrawable();
        canvas.save();
        Paint.FontMetricsInt paintMetrics = paint.getFontMetricsInt();
        final int fontHeight = paintMetrics.descent - paintMetrics.ascent;
        final int yCenter = y + paintMetrics.descent - fontHeight / 2;
        final Rect drawBounds = drawable.getBounds();
        final int translateY = yCenter - (drawBounds.bottom - drawBounds.top) / 2;
        canvas.translate(x, translateY);
        drawable.draw(canvas);
        canvas.restore();
    }
}