package app.revanced.extension.youtube.returnyoutubedislike;

import static app.revanced.extension.shared.StringRef.str;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.icu.text.CompactDecimalFormat;
import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import android.icu.text.NumberFormat;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.ReplacementSpan;
import android.widget.Toast;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.ui.Dim;
import app.revanced.extension.youtube.returnyoutubedislike.requests.RYDVoteData;
import app.revanced.extension.youtube.returnyoutubedislike.requests.ReturnYouTubeDislikeApi;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.PlayerType;

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
    private static final long MAX_MILLISECONDS_TO_BLOCK_UI_WAITING_FOR_FETCH = 4000;

    /**
     * How long to retain successful RYD fetches.
     */
    private static final long CACHE_TIMEOUT_SUCCESS_MILLISECONDS = 7 * 60 * 1000; // 7 Minutes

    /**
     * How long to retain unsuccessful RYD fetches,
     * and also the minimum time before retrying again.
     */
    private static final long CACHE_TIMEOUT_FAILURE_MILLISECONDS = 3 * 60 * 1000; // 3 Minutes

    /**
     * Unique placeholder character, used to detect if a segmented span already has dislikes added to it.
     * Must be something YouTube is unlikely to use, as it's searched for in all usage of Rolling Number.
     */
    private static final char MIDDLE_SEPARATOR_CHARACTER = '◎'; // 'bullseye'

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
    public static final Rect leftSeparatorBounds;
    private static final Rect middleSeparatorBounds;

    /**
     * Horizontal padding between the left and middle separator.
     */
    public static final int leftSeparatorShapePaddingPixels;
    private static final ShapeDrawable leftSeparatorShape;

    static {
        leftSeparatorBounds = new Rect(0, 0,
                Dim.dp(1.2f),
                Dim.dp(14f));
        final int middleSeparatorSize = Dim.dp(3.7f);
        middleSeparatorBounds = new Rect(0, 0, middleSeparatorSize, middleSeparatorSize);

        leftSeparatorShapePaddingPixels = Dim.dp(8.4f);

        leftSeparatorShape = new ShapeDrawable(new RectShape());
        leftSeparatorShape.setBounds(leftSeparatorBounds);
    }

    private final String videoId;

    /**
     * Stores the results of the vote api fetch, and used as a barrier to wait until fetch completes.
     * Absolutely cannot be holding any lock during calls to {@link Future#get()}.
     */
    private final Future<RYDVoteData> future;

    /**
     * Time this instance and the fetch future was created.
     */
    private final long timeFetched;

    /**
     * If this instance was previously used for a Short.
     */
    @GuardedBy("this")
    private boolean isShort;

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
     * Color of the left and middle separator, based on the color of the right separator.
     * It's unknown where YT gets the color from, and the values here are approximated by hand.
     * Ideally, this would be the actual color YT uses at runtime.
     */
    private static int getSeparatorColor() {
        return Utils.isDarkModeEnabled()
                ? 0x33FFFFFF
                : 0xFFD9D9D9;
    }

    public static ShapeDrawable getLeftSeparatorDrawable() {
        leftSeparatorShape.getPaint().setColor(getSeparatorColor());
        return leftSeparatorShape;
    }

    /**
     * @param isSegmentedButton If UI is using the segmented single UI component for both like and dislike.
     */
    @NonNull
    private static SpannableString createDislikeSpan(@NonNull Spanned oldSpannable,
                                                     boolean isSegmentedButton,
                                                     boolean isRollingNumber,
                                                     @NonNull RYDVoteData voteData) {
        if (!isSegmentedButton) {
            // Simple replacement of 'dislike' with a number/percentage.
            return newSpannableWithDislikes(oldSpannable, voteData);
        }

        // Note: Some locales use right to left layout (Arabic, Hebrew, etc).
        // If making changes to this code, change device settings to a RTL language and verify layout is correct.
        CharSequence oldLikes = oldSpannable;

        // YouTube creators can hide the like count on a video,
        // and the like count appears as a device language specific string that says 'Like'.
        // Check if the string contains any numbers.
        if (!Utils.containsNumber(oldLikes)) {
            // Likes are hidden by video creator
            //
            // RYD does not directly provide like data, but can use an estimated likes
            // using the same scale factor RYD applied to the raw dislikes.
            //
            // example video: https://www.youtube.com/watch?v=UnrU5vxCHxw
            // RYD data: https://returnyoutubedislikeapi.com/votes?videoId=UnrU5vxCHxw
            //
            if (!Settings.RYD_ESTIMATED_LIKE.get()) {
                // Change the "Likes" string to show that likes and dislikes are hidden.
                String hiddenMessageString = str("revanced_ryd_video_likes_hidden_by_video_owner");
                return newSpanUsingStylingOfAnotherSpan(oldSpannable, hiddenMessageString);
            }

            Logger.printDebug(() -> "Using estimated likes");
            oldLikes = formatDislikeCount(voteData.getLikeCount());
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        final boolean compactLayout = Settings.RYD_COMPACT_LAYOUT.get();

        if (!compactLayout) {
            String leftSeparatorString = Utils.getTextDirectionString();
            final Spannable leftSeparatorSpan;
            if (isRollingNumber) {
                leftSeparatorSpan = new SpannableString(leftSeparatorString);
            } else {
                leftSeparatorString += "  ";
                leftSeparatorSpan = new SpannableString(leftSeparatorString);
                // Styling spans cannot overwrite RTL or LTR character.
                leftSeparatorSpan.setSpan(
                        new VerticallyCenteredImageSpan(getLeftSeparatorDrawable(), false),
                        1, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                leftSeparatorSpan.setSpan(
                        new FixedWidthEmptySpan(leftSeparatorShapePaddingPixels),
                        2, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            builder.append(leftSeparatorSpan);
        }

        // likes
        builder.append(newSpanUsingStylingOfAnotherSpan(oldSpannable, oldLikes));

        // middle separator
        String middleSeparatorString = compactLayout
                ? "  " + MIDDLE_SEPARATOR_CHARACTER + "  "
                : "  \u2009" + MIDDLE_SEPARATOR_CHARACTER + "\u2009  "; // u2009 = 'narrow space' character
        final int shapeInsertionIndex = middleSeparatorString.length() / 2;
        Spannable middleSeparatorSpan = new SpannableString(middleSeparatorString);
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        shapeDrawable.getPaint().setColor(getSeparatorColor());
        shapeDrawable.setBounds(middleSeparatorBounds);
        // Use original text width if using Rolling Number,
        // to ensure the replacement styled span has the same width as the measured String,
        // otherwise layout can be broken (especially on devices with small system font sizes).
        middleSeparatorSpan.setSpan(
                new VerticallyCenteredImageSpan(shapeDrawable, isRollingNumber),
                shapeInsertionIndex, shapeInsertionIndex + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.append(middleSeparatorSpan);

        // dislikes
        builder.append(newSpannableWithDislikes(oldSpannable, voteData));

        return new SpannableString(builder);
    }

    /**
     * @return If the text is likely for a previously created likes/dislikes segmented span.
     */
    public static boolean isPreviouslyCreatedSegmentedSpan(@NonNull String text) {
        return text.indexOf(MIDDLE_SEPARATOR_CHARACTER) >= 0;
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

    private static SpannableString newSpannableWithLikes(@NonNull Spanned sourceStyling, @NonNull RYDVoteData voteData) {
        return newSpanUsingStylingOfAnotherSpan(sourceStyling, formatDislikeCount(voteData.getLikeCount()));
    }

    private static SpannableString newSpannableWithDislikes(@NonNull Spanned sourceStyling, @NonNull RYDVoteData voteData) {
        return newSpanUsingStylingOfAnotherSpan(sourceStyling,
                Settings.RYD_DISLIKE_PERCENTAGE.get()
                        ? formatDislikePercentage(voteData.getDislikePercentage())
                        : formatDislikeCount(voteData.getDislikeCount()));
    }

    private static SpannableString newSpanUsingStylingOfAnotherSpan(@NonNull Spanned sourceStyle, @NonNull CharSequence newSpanText) {
        if (sourceStyle == newSpanText && sourceStyle instanceof SpannableString) {
            return (SpannableString) sourceStyle; // Nothing to do.
        }

        SpannableString destination = new SpannableString(newSpanText);
        Object[] spans = sourceStyle.getSpans(0, sourceStyle.length(), Object.class);
        for (Object span : spans) {
            destination.setSpan(span, 0, destination.length(), sourceStyle.getSpanFlags(span));
        }

        return destination;
    }

    private static String formatDislikeCount(long dislikeCount) {
        synchronized (ReturnYouTubeDislike.class) { // Number formatter is not thread safe.
            if (dislikeCountFormatter == null) {
                // Must use default locale and not Utils context locale,
                // otherwise if using a different settings language then the
                // formatting will use that of the different language.
                Locale locale = Locale.getDefault();
                dislikeCountFormatter = CompactDecimalFormat.getInstance(locale, CompactDecimalFormat.CompactStyle.SHORT);

                // YouTube disregards locale specific number characters
                // and instead shows English number characters everywhere.
                // To use the same behavior, override the digit characters to use English
                // so languages such as Arabic will show "1.234" instead of the native "۱,۲۳٤"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
                    symbols.setDigitStrings(DecimalFormatSymbols.getInstance(Locale.ENGLISH).getDigitStrings());
                    dislikeCountFormatter.setDecimalFormatSymbols(symbols);
                }
            }

            return dislikeCountFormatter.format(dislikeCount);
        }
    }

    private static String formatDislikePercentage(float dislikePercentage) {
        synchronized (ReturnYouTubeDislike.class) { // Number formatter is not thread safe, must synchronize.
            if (dislikePercentageFormatter == null) {
                Locale locale = Locale.getDefault();
                dislikePercentageFormatter = NumberFormat.getPercentInstance(locale);

                // Want to set the digit strings, and the simplest way is to cast to the implementation NumberFormat returns.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                        && dislikePercentageFormatter instanceof DecimalFormat decimalFormatter) {
                    DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
                    symbols.setDigitStrings(DecimalFormatSymbols.getInstance(Locale.ENGLISH).getDigitStrings());
                    decimalFormatter.setDecimalFormatSymbols(symbols);
                }
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
            final long now = System.currentTimeMillis();
            fetchCache.values().removeIf(value -> {
                final boolean expired = value.isExpired(now);
                if (expired)
                    Logger.printDebug(() -> "Removing expired fetch: " + value.videoId);
                return expired;
            });

            ReturnYouTubeDislike fetch = fetchCache.get(videoId);
            if (fetch == null) {
                fetch = new ReturnYouTubeDislike(videoId);
                fetchCache.put(videoId, fetch);
            }
            return fetch;
        }
    }

    /**
     * Should be called if the user changes dislikes appearance settings.
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
        this.future = Utils.submitOnBackgroundThread(() -> ReturnYouTubeDislikeApi.fetchVotes(videoId));
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
            Logger.printDebug(() -> "Waited but future was not complete after: " + maxTimeToWait + "ms");
        } catch (ExecutionException | InterruptedException ex) {
            Logger.printException(() -> "Future failure ", ex); // will never happen
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
            Logger.printDebug(() -> "Clearing replacement span for: " + videoId);
        }
        replacementLikeDislikeSpan = null;
    }

    @NonNull
    public String getVideoId() {
        return videoId;
    }

    /**
     * Pre-emptively set this as a Short.
     */
    public synchronized void setVideoIdIsShort(boolean isShort) {
        this.isShort = isShort;
    }

    /**
     * @return the replacement span containing dislikes, or the original span if RYD is not available.
     */
    @NonNull
    public synchronized Spanned getDislikesSpanForRegularVideo(@NonNull Spanned original,
                                                               boolean isSegmentedButton,
                                                               boolean isRollingNumber) {
        return waitForFetchAndUpdateReplacementSpan(original, isSegmentedButton,
                isRollingNumber, false, false);
    }

    /**
     * Called when a Shorts like Spannable is created.
     */
    @NonNull
    public synchronized Spanned getLikeSpanForShort(@NonNull Spanned original) {
        return waitForFetchAndUpdateReplacementSpan(original, false,
                false, true, true);
    }

    /**
     * Called when a Shorts dislike Spannable is created.
     */
    @NonNull
    public synchronized Spanned getDislikeSpanForShort(@NonNull Spanned original) {
        return waitForFetchAndUpdateReplacementSpan(original, false,
                false, true, false);
    }

    @NonNull
    private Spanned waitForFetchAndUpdateReplacementSpan(@NonNull Spanned original,
                                                         boolean isSegmentedButton,
                                                         boolean isRollingNumber,
                                                         boolean spanIsForShort,
                                                         boolean spanIsForLikes) {
        try {
            RYDVoteData votingData = getFetchData(MAX_MILLISECONDS_TO_BLOCK_UI_WAITING_FOR_FETCH);
            if (votingData == null) {
                // Method automatically prevents showing multiple toasts if the connection failed.
                // This call is needed here in case the api call did succeed but took too long.
                ReturnYouTubeDislikeApi.handleConnectionError(
                        str("revanced_ryd_failure_connection_timeout"),
                        null, null, Toast.LENGTH_SHORT);
                Logger.printDebug(() -> "Cannot add dislike to UI (RYD data not available)");
                return original;
            }

            synchronized (this) {
                if (spanIsForShort) {
                    // Cannot set this to false if span is not for a Short.
                    // When spoofing to an old version and a Short is opened while a regular video
                    // is on screen, this instance can be loaded for the minimized regular video.
                    // But this Shorts data won't be displayed for that call
                    // and when it is un-minimized it will reload again and the load will be ignored.
                    isShort = true;
                } else if (isShort) {
                    // user:
                    // 1, opened a video
                    // 2. opened a short (without closing the regular video)
                    // 3. closed the short
                    // 4. regular video is now present, but the videoId and RYD data is still for the short
                    Logger.printDebug(() -> "Ignoring regular video dislike span,"
                            + " as data loaded was previously used for a Short: " + videoId);
                    return original;
                }

                if (spanIsForLikes) {
                    if (!Utils.containsNumber(original)) {
                        if (!Settings.RYD_ESTIMATED_LIKE.get()) {
                            Logger.printDebug(() -> "Likes are hidden");
                            return original;
                        } else {
                            Logger.printDebug(() -> "Using estimated likes");
                        }
                    }

                    // Scrolling Shorts does not cause the Spans to be reloaded,
                    // so there is no need to cache the likes for this situations.
                    Logger.printDebug(() -> "Creating likes span for: " + votingData.videoId);
                    return newSpannableWithLikes(original, votingData);
                }

                if (originalDislikeSpan != null && replacementLikeDislikeSpan != null
                        && spansHaveEqualTextAndColor(original, originalDislikeSpan)) {
                    Logger.printDebug(() -> "Replacing span with previously created dislike span of data: " + videoId);
                    return replacementLikeDislikeSpan;
                }

                // No replacement span exist, create it now.

                if (userVote != null) {
                    votingData.updateUsingVote(userVote);
                }
                originalDislikeSpan = original;
                replacementLikeDislikeSpan = createDislikeSpan(original, isSegmentedButton, isRollingNumber, votingData);
                Logger.printDebug(() -> "Replaced: '" + originalDislikeSpan + "' with: '"
                        + replacementLikeDislikeSpan + "'" + " using video: " + videoId);

                return replacementLikeDislikeSpan;
            }
        } catch (Exception ex) {
            Logger.printException(() -> "waitForFetchAndUpdateReplacementSpan failure", ex);
        }

        return original;
    }

    public void sendVote(@NonNull Vote vote) {
        Utils.verifyOnMainThread();
        Objects.requireNonNull(vote);

        try {
            PlayerType currentType = PlayerType.getCurrent();
            if (isShort != currentType.isNoneHiddenOrMinimized()) {
                Logger.printDebug(() -> "Cannot vote for video: " + videoId
                        + " as current player type does not match: " + currentType);

                // Shorts was loaded with regular video present, then Shorts was closed.
                // and then user voted on the now visible original video.
                // Cannot send a vote, because this instance is for the wrong video.
                Utils.showToastLong(str("revanced_ryd_failure_ryd_enabled_while_playing_video_then_user_voted"));
                return;
            }

            setUserVote(vote);

            voteSerialExecutor.execute(() -> {
                try { // Must wrap in try/catch to properly log exceptions.
                    ReturnYouTubeDislikeApi.sendVote(videoId, vote);
                } catch (Exception ex) {
                    Logger.printException(() -> "Failed to send vote", ex);
                }
            });
        } catch (Exception ex) {
            Logger.printException(() -> "Error trying to send vote", ex);
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
            Logger.printDebug(() -> "setUserVote: " + vote);

            synchronized (this) {
                userVote = vote;
                clearUICache();
            }

            if (future.isDone()) {
                // Update the fetched vote data.
                RYDVoteData voteData = getFetchData(MAX_MILLISECONDS_TO_BLOCK_UI_WAITING_FOR_FETCH);
                if (voteData == null) {
                    // RYD fetch failed.
                    Logger.printDebug(() -> "Cannot update UI (vote data not available)");
                    return;
                }
                voteData.updateUsingVote(vote);
            } // Else, vote will be applied after fetch completes.

        } catch (Exception ex) {
            Logger.printException(() -> "setUserVote failure", ex);
        }
    }
}

/**
 * Styles a Spannable with an empty fixed width.
 */
class FixedWidthEmptySpan extends ReplacementSpan {
    final int fixedWidth;
    /**
     * @param fixedWith Fixed width in screen pixels.
     */
    FixedWidthEmptySpan(int fixedWith) {
        this.fixedWidth = fixedWith;
        if (fixedWith < 0) throw new IllegalArgumentException();
    }
    @Override
    public int getSize(@NonNull Paint paint, @NonNull CharSequence text,
                       int start, int end, @Nullable Paint.FontMetricsInt fontMetrics) {
        return fixedWidth;
    }
    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, @NonNull Paint paint) {
        // Nothing to draw.
    }
}

/**
 * Vertically centers a Spanned Drawable.
 */
class VerticallyCenteredImageSpan extends ImageSpan {
    final boolean useOriginalWidth;

    /**
     * @param useOriginalWidth Use the original layout width of the text this span is applied to,
     * and not the bounds of the Drawable. Drawable is always displayed using it's own bounds,
     * and this setting only affects the layout width of the entire span.
     */
    public VerticallyCenteredImageSpan(Drawable drawable, boolean useOriginalWidth) {
        super(drawable);
        this.useOriginalWidth = useOriginalWidth;
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
            final int halfDrawHeight = drawHeight / 2;
            final int yCenter = paintMetrics.ascent + fontHeight / 2;

            fontMetrics.ascent = yCenter - halfDrawHeight;
            fontMetrics.top = fontMetrics.ascent;
            fontMetrics.bottom = yCenter + halfDrawHeight;
            fontMetrics.descent = fontMetrics.bottom;
        }
        if (useOriginalWidth) {
            return (int) paint.measureText(text, start, end);
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
        float translateX = x;
        if (useOriginalWidth) {
            // Horizontally center the drawable in the same space as the original text.
            translateX += (paint.measureText(text, start, end) - (drawBounds.right - drawBounds.left)) / 2;
        }
        final int translateY = yCenter - (drawBounds.bottom - drawBounds.top) / 2;
        canvas.translate(translateX, translateY);
        drawable.draw(canvas);
        canvas.restore();
    }
}