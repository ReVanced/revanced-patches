package app.revanced.extension.youtube.patches;

import static app.revanced.extension.youtube.returnyoutubedislike.ReturnYouTubeDislike.Vote;

import android.graphics.drawable.ShapeDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.patches.components.ReturnYouTubeDislikeFilter;
import app.revanced.extension.youtube.returnyoutubedislike.ReturnYouTubeDislike;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.PlayerType;

/**
 * Handles all interaction of UI patch components.
 *
 * Known limitation:
 * The implementation of Shorts litho requires blocking the loading the first Short until RYD has completed.
 * This is because it modifies the dislikes text synchronously, and if the RYD fetch has
 * not completed yet then the UI will be temporarily frozen.
 *
 * A (yet to be implemented) solution that fixes this problem.  Any one of:
 * - Modify patch to hook onto the Shorts Litho TextView, and update the dislikes text asynchronously.
 * - Find a way to force Litho to rebuild it's component tree,
 *   and use that hook to force the shorts dislikes to update after the fetch is completed.
 * - Hook into the dislikes button image view, and replace the dislikes thumb down image with a
 *   generated image of the number of dislikes, then update the image asynchronously.  This Could
 *   also be used for the regular video player to give a better UI layout and completely remove
 *   the need for the Rolling Number patches.
 */
@SuppressWarnings("unused")
public class ReturnYouTubeDislikePatch {

    /**
     * RYD data for the current video on screen.
     */
    @Nullable
    private static volatile ReturnYouTubeDislike currentVideoData;

    /**
     * The last litho based Shorts loaded.
     * May be the same value as {@link #currentVideoData}, but usually is the next short to swipe to.
     */
    @Nullable
    private static volatile ReturnYouTubeDislike lastLithoShortsVideoData;

    /**
     * Because litho Shorts spans are created offscreen after {@link ReturnYouTubeDislikeFilter}
     * detects the video ids, but the current Short can arbitrarily reload the same span,
     * then use the {@link #lastLithoShortsVideoData} if this value is greater than zero.
     */
    @GuardedBy("ReturnYouTubeDislikePatch.class")
    private static int useLithoShortsVideoDataCount;

    /**
     * Last video id prefetched. Field is to prevent prefetching the same video id multiple times in a row.
     */
    @Nullable
    private static volatile String lastPrefetchedVideoId;

    private static void clearData() {
        currentVideoData = null;
        lastLithoShortsVideoData = null;
        synchronized (ReturnYouTubeDislike.class) {
            useLithoShortsVideoDataCount = 0;
        }

        // Rolling number text should not be cleared,
        // as it's used if incognito Short is opened/closed
        // while a regular video is on screen.
    }

    /**
     * @return If {@link #useLithoShortsVideoDataCount} was greater than zero.
     */
    private static boolean decrementUseLithoDataIfNeeded() {
        synchronized (ReturnYouTubeDislikePatch.class) {
            if (useLithoShortsVideoDataCount > 0) {
                useLithoShortsVideoDataCount--;
                return true;
            }

            return false;
        }
    }

    //
    // Litho player for both regular videos and Shorts.
    //

    /**
     * Injection point.
     *
     * For Litho segmented buttons and Litho Shorts player.
     */
    @NonNull
    public static CharSequence onLithoTextLoaded(@NonNull Object conversionContext,
                                                 @NonNull CharSequence original) {
        return onLithoTextLoaded(conversionContext, original, false);
    }

    /**
     * Called when a litho text component is initially created,
     * and also when a Span is later reused again (such as scrolling off/on screen).
     *
     * This method is sometimes called on the main thread, but it usually is called _off_ the main thread.
     * This method can be called multiple times for the same UI element (including after dislikes was added).
     *
     * @param original Original char sequence was created or reused by Litho.
     * @param isRollingNumber If the span is for a Rolling Number.
     * @return The original char sequence (if nothing should change), or a replacement char sequence that contains dislikes.
     */
    @NonNull
    private static CharSequence onLithoTextLoaded(@NonNull Object conversionContext,
                                                  @NonNull CharSequence original,
                                                  boolean isRollingNumber) {
        try {
            if (!Settings.RYD_ENABLED.get()) {
                return original;
            }

            String conversionContextString = conversionContext.toString();

            if (isRollingNumber && !conversionContextString.contains("video_action_bar.e")) {
                return original;
            }

            if (conversionContextString.contains("segmented_like_dislike_button.e")) {
                // Regular video.
                ReturnYouTubeDislike videoData = currentVideoData;
                if (videoData == null) {
                    return original; // User enabled RYD while a video was on screen.
                }
                if (!(original instanceof Spanned)) {
                    original = new SpannableString(original);
                }
                return videoData.getDislikesSpanForRegularVideo((Spanned) original,
                        true, isRollingNumber);
            }

            if (isRollingNumber) {
                return original; // No need to check for Shorts in the context.
            }

            if (Utils.containsAny(conversionContextString,
                    "|shorts_dislike_button.e", "|reel_dislike_button.e")) {
                return getShortsSpan(original, true);
            }

            if (Utils.containsAny(conversionContextString,
                    "|shorts_like_button.e", "|reel_like_button.e")) {
                if (!Utils.containsNumber(original)) {
                    Logger.printDebug(() -> "Replacing hidden likes count");
                    return getShortsSpan(original, false);
                } else {
                    decrementUseLithoDataIfNeeded();
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "onLithoTextLoaded failure", ex);
        }
        return original;
    }

    private static CharSequence getShortsSpan(@NonNull CharSequence original, boolean isDislikesSpan) {
        // Litho Shorts player.
        if (!Settings.RYD_SHORTS.get() || (isDislikesSpan && Settings.HIDE_SHORTS_DISLIKE_BUTTON.get())
                || (!isDislikesSpan && Settings.HIDE_SHORTS_LIKE_BUTTON.get())) {
            return original;
        }

        final ReturnYouTubeDislike videoData;
        if (decrementUseLithoDataIfNeeded()) {
            // New Short is loading off screen.
            videoData = lastLithoShortsVideoData;
        } else {
            videoData = currentVideoData;
        }

        if (videoData == null) {
            // The Shorts litho video id filter did not detect the video id.
            // This is normal in incognito mode, but otherwise is abnormal.
            Logger.printDebug(() -> "Cannot modify Shorts litho span, data is null");
            return original;
        }

        return isDislikesSpan
                ? videoData.getDislikeSpanForShort((Spanned) original)
                : videoData.getLikeSpanForShort((Spanned) original);
    }

    //
    // Rolling Number
    //

    /**
     * Current regular video rolling number text, if rolling number is in use.
     * This is saved to a field as it's used in every draw() call.
     */
    @Nullable
    private static volatile CharSequence rollingNumberSpan;

    /**
     * Injection point.
     */
    public static String onRollingNumberLoaded(@NonNull Object conversionContext,
                                               @NonNull String original) {
        try {
            CharSequence replacement = onLithoTextLoaded(conversionContext, original, true);

            String replacementString = replacement.toString();
            if (!replacementString.equals(original)) {
                rollingNumberSpan = replacement;
                return replacementString;
            } // Else, the text was not a likes count but instead the view count or something else.
        } catch (Exception ex) {
            Logger.printException(() -> "onRollingNumberLoaded failure", ex);
        }
        return original;
    }

    /**
     * Injection point.
     *
     * Called for all usage of Rolling Number.
     * Modifies the measured String text width to include the left separator and padding, if needed.
     */
    public static float onRollingNumberMeasured(String text, float measuredTextWidth) {
        try {
            if (Settings.RYD_ENABLED.get()) {
                if (ReturnYouTubeDislike.isPreviouslyCreatedSegmentedSpan(text)) {
                    // +1 pixel is needed for some foreign languages that measure
                    // the text different from what is used for layout (Greek in particular).
                    // Probably a bug in Android, but who knows.
                    // Single line mode is also used as an additional fix for this issue.
                    if (Settings.RYD_COMPACT_LAYOUT.get()) {
                        return measuredTextWidth + 1;
                    }

                    return measuredTextWidth + 1
                            + ReturnYouTubeDislike.leftSeparatorBounds.right
                            + ReturnYouTubeDislike.leftSeparatorShapePaddingPixels;
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "onRollingNumberMeasured failure", ex);
        }

        return measuredTextWidth;
    }

    /**
     * Add Rolling Number text view modifications.
     */
    private static void addRollingNumberPatchChanges(TextView view) {
        // YouTube Rolling Numbers do not use compound drawables or drawable padding.
        if (view.getCompoundDrawablePadding() == 0) {
            Logger.printDebug(() -> "Adding rolling number TextView changes");
            view.setCompoundDrawablePadding(ReturnYouTubeDislike.leftSeparatorShapePaddingPixels);
            ShapeDrawable separator = ReturnYouTubeDislike.getLeftSeparatorDrawable();
            if (Utils.isRightToLeftLocale()) {
                view.setCompoundDrawables(null, null, separator, null);
            } else {
                view.setCompoundDrawables(separator, null, null, null);
            }

            // Disliking can cause the span to grow in size, which is ok and is laid out correctly,
            // but if the user then removes their dislike the layout will not adjust to the new shorter width.
            // Use a center alignment to take up any extra space.
            view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            // Single line mode does not clip words if the span is larger than the view bounds.
            // The styled span applied to the view should always have the same bounds,
            // but use this feature just in case the measurements are somehow off by a few pixels.
            view.setSingleLine(true);
        }
    }

    /**
     * Remove Rolling Number text view modifications made by this patch.
     * Required as it appears text views can be reused for other rolling numbers (view count, upload time, etc).
     */
    private static void removeRollingNumberPatchChanges(TextView view) {
        if (view.getCompoundDrawablePadding() != 0) {
            Logger.printDebug(() -> "Removing rolling number TextView changes");
            view.setCompoundDrawablePadding(0);
            view.setCompoundDrawables(null, null, null, null);
            view.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY); // Default alignment
            view.setSingleLine(false);
        }
    }

    /**
     * Injection point.
     */
    public static CharSequence updateRollingNumber(TextView view, CharSequence original) {
        try {
            if (!Settings.RYD_ENABLED.get()) {
                removeRollingNumberPatchChanges(view);
                return original;
            }
            // Called for all instances of RollingNumber, so must check if text is for a dislikes.
            // Text will already have the correct content but it's missing the drawable separators.
            if (!ReturnYouTubeDislike.isPreviouslyCreatedSegmentedSpan(original.toString())) {
                // The text is the video view count, upload time, or some other text.
                removeRollingNumberPatchChanges(view);
                return original;
            }

            CharSequence replacement = rollingNumberSpan;
            if (replacement == null) {
                // User enabled RYD while a video was open,
                // or user opened/closed a Short while a regular video was opened.
                Logger.printDebug(() -> "Cannot update rolling number (field is null");
                removeRollingNumberPatchChanges(view);
                return original;
            }

            if (Settings.RYD_COMPACT_LAYOUT.get()) {
                removeRollingNumberPatchChanges(view);
            } else {
                addRollingNumberPatchChanges(view);
            }

            // Remove any padding set by Rolling Number.
            view.setPadding(0, 0, 0, 0);

            // When displaying dislikes, the rolling animation is not visually correct
            // and the dislikes always animate (even though the dislike count has not changed).
            // The animation is caused by an image span attached to the span,
            // and using only the modified segmented span prevents the animation from showing.
            return replacement;
        } catch (Exception ex) {
            Logger.printException(() -> "updateRollingNumber failure", ex);
            return original;
        }
    }

    //
    // Video Id and voting hooks (all players).
    //

    private static volatile boolean lastPlayerResponseWasShort;

    /**
     * Injection point.  Uses 'playback response' video id hook to preload RYD.
     */
    public static void preloadVideoId(@NonNull String videoId, boolean isShortAndOpeningOrPlaying) {
        try {
            if (!Settings.RYD_ENABLED.get()) {
                return;
            }
            if (videoId.equals(lastPrefetchedVideoId)) {
                return;
            }
            if (!Utils.isNetworkConnected()) {
                Logger.printDebug(() -> "Cannot pre-fetch RYD, network is not connected");
                lastPrefetchedVideoId = null;
                return;
            }

            final boolean videoIdIsShort = VideoInformation.lastPlayerResponseIsShort();
            // Shorts shelf in home and subscription feed causes player response hook to be called,
            // and the 'is opening/playing' parameter will be false.
            // This hook will be called again when the Short is actually opened.
            if (videoIdIsShort && (!isShortAndOpeningOrPlaying || !Settings.RYD_SHORTS.get())) {
                return;
            }
            final boolean waitForFetchToComplete = videoIdIsShort && !lastPlayerResponseWasShort;

            Logger.printDebug(() -> "Prefetching RYD for video: " + videoId);
            ReturnYouTubeDislike fetch = ReturnYouTubeDislike.getFetchForVideoId(videoId);
            if (waitForFetchToComplete && !fetch.fetchCompleted()) {
                // This call is off the main thread, so wait until the RYD fetch completely finishes,
                // otherwise if this returns before the fetch completes then the UI can
                // become frozen when the main thread tries to modify the litho Shorts dislikes and
                // it must wait for the fetch.
                // Only need to do this for the first Short opened, as the next Short to swipe to
                // are preloaded in the background.
                //
                // If an asynchronous litho Shorts solution is found, then this blocking call should be removed.
                Logger.printDebug(() -> "Waiting for prefetch to complete: " + videoId);
                fetch.getFetchData(20000); // Any arbitrarily large max wait time.
            }

            // Set the fields after the fetch completes, so any concurrent calls will also wait.
            lastPlayerResponseWasShort = videoIdIsShort;
            lastPrefetchedVideoId = videoId;
        } catch (Exception ex) {
            Logger.printException(() -> "preloadVideoId failure", ex);
        }
    }

    /**
     * Injection point.  Uses 'current playing' video id hook.  Always called on main thread.
     */
    public static void newVideoLoaded(@NonNull String videoId) {
        try {
            if (!Settings.RYD_ENABLED.get()) return;
            Objects.requireNonNull(videoId);

            PlayerType currentPlayerType = PlayerType.getCurrent();
            final boolean isNoneHiddenOrSlidingMinimized = currentPlayerType.isNoneHiddenOrSlidingMinimized();
            if (isNoneHiddenOrSlidingMinimized && !Settings.RYD_SHORTS.get()) {
                // Must clear here, otherwise the wrong data can be used for a minimized regular video.
                clearData();
                return;
            }

            if (videoIdIsSame(currentVideoData, videoId)) {
                return;
            }
            Logger.printDebug(() -> "New video id: " + videoId + " playerType: " + currentPlayerType);

            if (!Utils.isNetworkConnected()) {
                Logger.printDebug(() -> "Cannot fetch RYD, network is not connected");
                currentVideoData = null;
                return;
            }

            ReturnYouTubeDislike data = ReturnYouTubeDislike.getFetchForVideoId(videoId);
            // Pre-emptively set the data to short status.
            // Required to prevent Shorts data from being used on a minimized video in incognito mode.
            if (isNoneHiddenOrSlidingMinimized) {
                data.setVideoIdIsShort(true);
            }
            currentVideoData = data;
        } catch (Exception ex) {
            Logger.printException(() -> "newVideoLoaded failure", ex);
        }
    }

    public static void setLastLithoShortsVideoId(@Nullable String videoId) {
        if (videoIdIsSame(lastLithoShortsVideoData, videoId)) {
            return;
        }

        if (videoId == null) {
            // Litho filter did not detect the video id.  App is in incognito mode,
            // or the proto buffer structure was changed and the video id is no longer present.
            // Must clear both currently playing and last litho data otherwise the
            // next regular video may use the wrong data.
            Logger.printDebug(() -> "Litho filter did not find any video ids");
            clearData();
            return;
        }

        Logger.printDebug(() -> "New litho Shorts video id: " + videoId);
        ReturnYouTubeDislike videoData = ReturnYouTubeDislike.getFetchForVideoId(videoId);
        videoData.setVideoIdIsShort(true);
        lastLithoShortsVideoData = videoData;
        synchronized (ReturnYouTubeDislikePatch.class) {
            // Use litho Shorts data for the next like and dislike spans.
            useLithoShortsVideoDataCount = 2;
        }
    }

    private static boolean videoIdIsSame(@Nullable ReturnYouTubeDislike fetch, @Nullable String videoId) {
        return (fetch == null && videoId == null)
                || (fetch != null && fetch.getVideoId().equals(videoId));
    }

    /**
     * Injection point.
     *
     * Called when the user likes or dislikes.
     *
     * @param vote int that matches {@link Vote#value}
     */
    public static void sendVote(int vote) {
        try {
            if (!Settings.RYD_ENABLED.get()) {
                return;
            }

            final boolean isNoneHiddenOrMinimized = PlayerType.getCurrent().isNoneHiddenOrMinimized();
            if (isNoneHiddenOrMinimized && !Settings.RYD_SHORTS.get()) {
                return;
            }

            ReturnYouTubeDislike videoData = currentVideoData;
            if (videoData == null) {
                Logger.printDebug(() -> "Cannot send vote, as current video data is null");
                return; // User enabled RYD while a regular video was minimized.
            }

            for (Vote v : Vote.values()) {
                if (v.value == vote) {
                    videoData.sendVote(v);
                    return;
                }
            }

            Logger.printException(() -> "Unknown vote type: " + vote);
        } catch (Exception ex) {
            Logger.printException(() -> "sendVote failure", ex);
        }
    }
}
