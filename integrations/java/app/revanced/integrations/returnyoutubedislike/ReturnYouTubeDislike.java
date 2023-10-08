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
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
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
 * Handles fetching and creation/replacing of RYD dislike text spans.
 *
 * Because Litho creates spans using multiple threads, this entire class supports multithreading as well.
 */
public class ReturnYouTubeDislike {

    public enum Vote {
        LIKE(1),
        DISLIKE(-1),
        LIKE_REMOVE(0);

        public final int value;

        Vote(int value) {
            this.value = value;
        }
    }

    /**
     * Maximum amount of time to block the UI from updates while waiting for network call to complete.
     *
     * Must be less than 5 seconds, as per:
     * https://developer.android.com/topic/performance/vitals/anr
     */
    private static final long MAX_MILLISECONDS_TO_BLOCK_UI_WAITING_FOR_FETCH = 4500;

    /**
     * How long to retain successful RYD fetches.
     */
    private static final long CACHE_TIMEOUT_SUCCESS_MILLISECONDS = 5 * 60 * 1000; // 5 Minutes

    /**
     * How long to retain unsuccessful RYD fetches,
     * and also the minimum time before retrying again.
     */
    private static final long CACHE_TIMEOUT_FAILURE_MILLISECONDS = 60 * 1000; // 1 Minute

    /**
     * Unique placeholder character, used to detect if a segmented span already has dislikes added to it.
     * Can be any almost any non-visible character.
     */
    private static final char MIDDLE_SEPARATOR_CHARACTER = '\u2009'; // 'narrow space' character

    /**
     * Cached lookup of all video ids.
     */
    @GuardedBy("itself")
    private static final Map<String, ReturnYouTubeDislike> fetchCache = new HashMap<>();

    /**
     * Used to send votes, one by one, in the same order the user created them.
     */
    private static final ExecutorService voteSerialExecutor = Executors.newSingleThreadExecutor();

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

    // Used for segmented dislike spans in Litho regular player.
    private static final Rect leftSeparatorBounds;
    private static final Rect middleSeparatorBounds;

    static {
        DisplayMetrics dp = Objects.requireNonNull(ReVancedUtils.getContext()).getResources().getDisplayMetrics();

        leftSeparatorBounds = new Rect(0, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.2f, dp),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 18, dp));
        final int middleSeparatorSize =
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3.7f, dp);
        middleSeparatorBounds = new Rect(0, 0, middleSeparatorSize, middleSeparatorSize);
    }

    private final String videoId;

    /**
     * Stores the results of the vote api fetch, and used as a barrier to wait until fetch completes.
     * Absolutely cannot be holding any lock during calls to {@link Future#get()}.
     */
    private final Future<RYDVoteData> future;

    /**
     * Time this instance and the future was created.
     */
    private final long timeFetched;

    /**
     * If the video id is for a Short.
     * Value of TRUE indicates it was previously loaded for a Short
     * and FALSE indicates a regular video.
     * NULL values means short status is not yet known.
     */
    @Nullable
    @GuardedBy("this")
    private Boolean isShort;

    /**
     * Optional current vote status of the UI.  Used to apply a user vote that was done on a previous video viewing.
     */
    @Nullable
    @GuardedBy("this")
    private Vote userVote;

    /**
     * Original dislike span, before modifications.
     */
    @Nullable
    @GuardedBy("this")
    private Spanned originalDislikeSpan;

    /**
     * Replacement like/dislike span that includes formatted dislikes.
     * Used to prevent recreating the same span multiple times.
     */
    @Nullable
    @GuardedBy("this")
    private SpannableString replacementLikeDislikeSpan;

    /**
     * @param isSegmentedButton If UI is using the segmented single UI component for both like and dislike.
     */
    @NonNull
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
        final boolean compactLayout = SettingsEnum.RYD_COMPACT_LAYOUT.getBoolean();
        final int separatorColor = ThemeHelper.isDarkTheme()
                ? 0x29AAAAAA  // transparent dark gray
                : 0xFFD9D9D9; // light gray

        if (!compactLayout) {
            // left separator
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

    // Alternatively, this could check if the span contains one of the custom created spans, but this is simple and quick.
    private static boolean isPreviouslyCreatedSegmentedSpan(@NonNull Spanned span) {
        return span.toString().indexOf(MIDDLE_SEPARATOR_CHARACTER) != -1;
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

    private static boolean spansHaveEqualTextAndColor(@NonNull Spanned one, @NonNull Spanned two) {
        // Cannot use equals on the span, because many of the inner styling spans do not implement equals.
        // Instead, compare the underlying text and the text color to handle when dark mode is changed.
        // Cannot compare the status of device dark mode, as Litho components are updated just before dark mode status changes.
        if (!one.toString().equals(two.toString())) {
            return false;
        }
        ForegroundColorSpan[] oneColors = one.getSpans(0, one.length(), ForegroundColorSpan.class);
        ForegroundColorSpan[] twoColors = two.getSpans(0, two.length(), ForegroundColorSpan.class);
        final int oneLength = oneColors.length;
        if (oneLength != twoColors.length) {
            return false;
        }
        for (int i = 0; i < oneLength; i++) {
            if (oneColors[i].getForegroundColor() != twoColors[i].getForegroundColor()) {
                return false;
            }
        }
        return true;
    }

    private static SpannableString newSpannableWithDislikes(@NonNull Spanned sourceStyling, @NonNull RYDVoteData voteData) {
        return newSpanUsingStylingOfAnotherSpan(sourceStyling,
                SettingsEnum.RYD_DISLIKE_PERCENTAGE.getBoolean()
                        ? formatDislikePercentage(voteData.getDislikePercentage())
                        : formatDislikeCount(voteData.getDislikeCount()));
    }

    private static SpannableString newSpanUsingStylingOfAnotherSpan(@NonNull Spanned sourceStyle, @NonNull CharSequence newSpanText) {
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

        // Will never be reached, as the oldest supported YouTube app requires Android N or greater.
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

    @NonNull
    public static ReturnYouTubeDislike getFetchForVideoId(@Nullable String videoId) {
        Objects.requireNonNull(videoId);
        synchronized (fetchCache) {
            // Remove any expired entries.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                final long now = System.currentTimeMillis();
                fetchCache.values().removeIf(value -> {
                    final boolean expired = value.isExpired(now);
                    if (expired)
                        LogHelper.printDebug(() -> "Removing expired fetch: " + value.videoId);
                    return expired;
                });
            }

            ReturnYouTubeDislike fetch = fetchCache.get(videoId);
            if (fetch == null) {
                fetch = new ReturnYouTubeDislike(videoId);
                fetchCache.put(videoId, fetch);
            }
            return fetch;
        }
    }

    /**
     * Should be called if the user changes settings for dislikes appearance.
     */
    public static void clearAllUICaches() {
        synchronized (fetchCache) {
            for (ReturnYouTubeDislike fetch : fetchCache.values()) {
                fetch.clearUICache();
            }
        }
    }

    private ReturnYouTubeDislike(@NonNull String videoId) {
        this.videoId = Objects.requireNonNull(videoId);
        this.timeFetched = System.currentTimeMillis();
        this.future = ReVancedUtils.submitOnBackgroundThread(() -> ReturnYouTubeDislikeApi.fetchVotes(videoId));
    }

    private boolean isExpired(long now) {
        final long timeSinceCreation = now - timeFetched;
        if (timeSinceCreation < CACHE_TIMEOUT_FAILURE_MILLISECONDS) {
            return false; // Not expired, even if the API call failed.
        }
        if (timeSinceCreation > CACHE_TIMEOUT_SUCCESS_MILLISECONDS) {
            return true; // Always expired.
        }
        // Only expired if the fetch failed (API null response).
        return (!fetchCompleted() || getFetchData(MAX_MILLISECONDS_TO_BLOCK_UI_WAITING_FOR_FETCH) == null);
    }

    @Nullable
    public RYDVoteData getFetchData(long maxTimeToWait) {
        try {
            return future.get(maxTimeToWait, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            LogHelper.printDebug(() -> "Waited but future was not complete after: " + maxTimeToWait + "ms");
        } catch (ExecutionException | InterruptedException ex) {
            LogHelper.printException(() -> "Future failure ", ex); // will never happen
        }
        return null;
    }

    /**
     * @return if the RYD fetch call has completed.
     */
    public boolean fetchCompleted() {
        return future.isDone();
    }

    private synchronized void clearUICache() {
        if (replacementLikeDislikeSpan != null) {
            LogHelper.printDebug(() -> "Clearing replacement span for: " + videoId);
        }
        replacementLikeDislikeSpan = null;
    }

    @NonNull
    public String getVideoId() {
        return videoId;
    }

    /**
     * Pre-emptively set this as a Short.
     * Should only be used immediately after creation of this instance.
     */
    public synchronized void setVideoIdIsShort(boolean isShort) {
        this.isShort = isShort;
    }

    /**
     * @return the replacement span containing dislikes, or the original span if RYD is not available.
     */
    @NonNull
    public synchronized Spanned getDislikesSpanForRegularVideo(@NonNull Spanned original, boolean isSegmentedButton) {
        return waitForFetchAndUpdateReplacementSpan(original, isSegmentedButton, false);
    }

    /**
     * Called when a Shorts dislike Spannable is created.
     */
    @NonNull
    public synchronized Spanned getDislikeSpanForShort(@NonNull Spanned original) {
        return waitForFetchAndUpdateReplacementSpan(original, false, true);
    }

    @NonNull
    private Spanned waitForFetchAndUpdateReplacementSpan(@NonNull Spanned original,
                                                         boolean isSegmentedButton,
                                                         boolean spanIsForShort) {
        try {
            RYDVoteData votingData = getFetchData(MAX_MILLISECONDS_TO_BLOCK_UI_WAITING_FOR_FETCH);
            if (votingData == null) {
                LogHelper.printDebug(() -> "Cannot add dislike to UI (RYD data not available)");
                return original;
            }

            synchronized (this) {
                if (isShort != null) {
                    if (isShort != spanIsForShort) {
                        // user:
                        // 1, opened a video
                        // 2. opened a short (without closing the regular video)
                        // 3. closed the short
                        // 4. regular video is now present, but the videoId and RYD data is still for the short
                        LogHelper.printDebug(() -> "Ignoring dislike span, as data loaded was previously"
                                + " used for a different video type.");
                        return original;
                    }
                } else {
                    isShort = spanIsForShort;
                }

                if (originalDislikeSpan != null && replacementLikeDislikeSpan != null) {
                    if (spansHaveEqualTextAndColor(original, replacementLikeDislikeSpan)) {
                        LogHelper.printDebug(() -> "Ignoring previously created dislikes span");
                        return original;
                    }
                    if (spansHaveEqualTextAndColor(original, originalDislikeSpan)) {
                        LogHelper.printDebug(() -> "Replacing span with previously created dislike span");
                        return replacementLikeDislikeSpan;
                    }
                }
                if (isSegmentedButton && isPreviouslyCreatedSegmentedSpan(original)) {
                    // need to recreate using original, as original has prior outdated dislike values
                    if (originalDislikeSpan == null) {
                        LogHelper.printDebug(() -> "Cannot add dislikes - original span is null"); // should never happen
                        return original;
                    }
                    original = originalDislikeSpan;
                }

                // No replacement span exist, create it now.

                if (userVote != null) {
                    votingData.updateUsingVote(userVote);
                }
                originalDislikeSpan = original;
                replacementLikeDislikeSpan = createDislikeSpan(original, isSegmentedButton, votingData);
                LogHelper.printDebug(() -> "Replaced: '" + originalDislikeSpan + "' with: '"
                        + replacementLikeDislikeSpan + "'" + " using video: " + videoId);

                return replacementLikeDislikeSpan;
            }
        } catch (Exception e) {
            LogHelper.printException(() -> "waitForFetchAndUpdateReplacementSpan failure", e); // should never happen
        }
        return original;
    }

    public void sendVote(@NonNull Vote vote) {
        ReVancedUtils.verifyOnMainThread();
        Objects.requireNonNull(vote);
        try {
            if (isShort != null && isShort != PlayerType.getCurrent().isNoneOrHidden()) {
                // Shorts was loaded with regular video present, then Shorts was closed.
                // and then user voted on the now visible original video.
                // Cannot send a vote, because the loaded videoId is for the wrong video.
                ReVancedUtils.showToastLong(str("revanced_ryd_failure_ryd_enabled_while_playing_video_then_user_voted"));
                return;
            }

            setUserVote(vote);

            voteSerialExecutor.execute(() -> {
                try { // Must wrap in try/catch to properly log exceptions.
                    ReturnYouTubeDislikeApi.sendVote(videoId, vote);
                } catch (Exception ex) {
                    LogHelper.printException(() -> "Failed to send vote", ex);
                }
            });
        } catch (Exception ex) {
            LogHelper.printException(() -> "Error trying to send vote", ex);
        }
    }

    /**
     * Sets the current user vote value, and does not send the vote to the RYD API.
     *
     * Only used to set value if thumbs up/down is already selected on video load.
     */
    public void setUserVote(@NonNull Vote vote) {
        Objects.requireNonNull(vote);
        try {
            LogHelper.printDebug(() -> "setUserVote: " + vote);

            synchronized (this) {
                userVote = vote;
                clearUICache();
            }
            
            if (future.isDone()) {
                // Update the fetched vote data.
                RYDVoteData voteData = getFetchData(MAX_MILLISECONDS_TO_BLOCK_UI_WAITING_FOR_FETCH);
                if (voteData == null) {
                    // RYD fetch failed.
                    LogHelper.printDebug(() -> "Cannot update UI (vote data not available)");
                    return;
                }
                voteData.updateUsingVote(vote);
            } // Else, vote will be applied after fetch completes.

        } catch (Exception ex) {
            LogHelper.printException(() -> "setUserVote failure", ex);
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