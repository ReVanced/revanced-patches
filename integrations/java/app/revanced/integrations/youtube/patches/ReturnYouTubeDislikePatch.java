package app.revanced.integrations.youtube.patches;

import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.os.Build;
import android.text.*;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.patches.components.ReturnYouTubeDislikeFilterPatch;
import app.revanced.integrations.youtube.patches.spoof.SpoofAppVersionPatch;
import app.revanced.integrations.youtube.returnyoutubedislike.ReturnYouTubeDislike;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.shared.PlayerType;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static app.revanced.integrations.youtube.returnyoutubedislike.ReturnYouTubeDislike.Vote;

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

    public static final boolean IS_SPOOFING_TO_NON_LITHO_SHORTS_PLAYER =
            SpoofAppVersionPatch.isSpoofingToEqualOrLessThan("18.33.40");

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
     * Because the litho Shorts spans are created after {@link ReturnYouTubeDislikeFilterPatch}
     * detects the video ids, after the user votes the litho will update
     * but {@link #lastLithoShortsVideoData} is not the correct data to use.
     * If this is true, then instead use {@link #currentVideoData}.
     */
    private static volatile boolean lithoShortsShouldUseCurrentData;

    /**
     * Last video id prefetched. Field is prevent prefetching the same video id multiple times in a row.
     */
    @Nullable
    private static volatile String lastPrefetchedVideoId;

    public static void onRYDStatusChange(boolean rydEnabled) {
        if (!rydEnabled) {
            // Must remove all values to protect against using stale data
            // if the user enables RYD while a video is on screen.
            clearData();
        }
    }

    private static void clearData() {
        currentVideoData = null;
        lastLithoShortsVideoData = null;
        lithoShortsShouldUseCurrentData = false;
        // Rolling number text should not be cleared,
        // as it's used if incognito Short is opened/closed
        // while a regular video is on screen.
    }

    //
    // 17.x non litho regular video player.
    //

    /**
     * Resource identifier of old UI dislike button.
     */
    private static final int OLD_UI_DISLIKE_BUTTON_RESOURCE_ID
            = Utils.getResourceIdentifier("dislike_button", "id");

    /**
     * Dislikes text label used by old UI.
     */
    @NonNull
    private static WeakReference<TextView> oldUITextViewRef = new WeakReference<>(null);

    /**
     * Original old UI 'Dislikes' text before patch modifications.
     * Required to reset the dislikes when changing videos and RYD is not available.
     * Set only once during the first load.
     */
    private static Spanned oldUIOriginalSpan;

    /**
     * Replacement span that contains dislike value. Used by {@link #oldUiTextWatcher}.
     */
    @Nullable
    private static Spanned oldUIReplacementSpan;

    /**
     * Old UI dislikes can be set multiple times by YouTube.
     * To prevent reverting changes made here, this listener overrides any future changes YouTube makes.
     */
    private static final TextWatcher oldUiTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        public void afterTextChanged(Editable s) {
            if (oldUIReplacementSpan == null || oldUIReplacementSpan.toString().equals(s.toString())) {
                return;
            }
            s.replace(0, s.length(), oldUIReplacementSpan); // Causes a recursive call back into this listener
        }
    };

    private static void updateOldUIDislikesTextView() {
        ReturnYouTubeDislike videoData = currentVideoData;
        if (videoData == null) {
            return;
        }
        TextView oldUITextView = oldUITextViewRef.get();
        if (oldUITextView == null) {
            return;
        }
        oldUIReplacementSpan = videoData.getDislikesSpanForRegularVideo(oldUIOriginalSpan, false, false);
        if (!oldUIReplacementSpan.equals(oldUITextView.getText())) {
            oldUITextView.setText(oldUIReplacementSpan);
        }
    }

    /**
     * Injection point.  Called on main thread.
     *
     * Used when spoofing to 16.x and 17.x versions.
     */
    public static void setOldUILayoutDislikes(int buttonViewResourceId, @Nullable TextView textView) {
        try {
            if (!Settings.RYD_ENABLED.get()
                    || buttonViewResourceId != OLD_UI_DISLIKE_BUTTON_RESOURCE_ID
                    || textView == null) {
                return;
            }
            Logger.printDebug(() -> "setOldUILayoutDislikes");

            if (oldUIOriginalSpan == null) {
                // Use value of the first instance, as it appears TextViews can be recycled
                // and might contain dislikes previously added by the patch.
                oldUIOriginalSpan = (Spanned) textView.getText();
            }
            oldUITextViewRef = new WeakReference<>(textView);
            // No way to check if a listener is already attached, so remove and add again.
            textView.removeTextChangedListener(oldUiTextWatcher);
            textView.addTextChangedListener(oldUiTextWatcher);

            /**
             * If the patch is changed to include the dislikes button as a parameter to this method,
             * then if the button is already selected the dislikes could be adjusted using
             * {@link ReturnYouTubeDislike#setUserVote(Vote)}
             */

            updateOldUIDislikesTextView();

        } catch (Exception ex) {
            Logger.printException(() -> "setOldUILayoutDislikes failure", ex);
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

            final CharSequence replacement;
            if (conversionContextString.contains("|segmented_like_dislike_button.eml|")) {
                // Regular video.
                ReturnYouTubeDislike videoData = currentVideoData;
                if (videoData == null) {
                    return original; // User enabled RYD while a video was on screen.
                }
                if (!(original instanceof Spanned)) {
                    original = new SpannableString(original);
                }
                replacement = videoData.getDislikesSpanForRegularVideo((Spanned) original,
                        true, isRollingNumber);

                // When spoofing between 17.09.xx and 17.30.xx the UI is the old layout
                // but uses litho and the dislikes is "|dislike_button.eml|".
                // But spoofing to that range gives a broken UI layout so no point checking for that.
            } else if (!isRollingNumber && conversionContextString.contains("|shorts_dislike_button.eml|")) {
                // Litho Shorts player.
                if (!Settings.RYD_SHORTS.get()) {
                    // Must clear the current video here, otherwise if the user opens a regular video
                    // then opens a litho short (while keeping the regular video on screen), then closes the short,
                    // the original video may show the incorrect dislike value.
                    clearData();
                    return original;
                }
                ReturnYouTubeDislike videoData = lastLithoShortsVideoData;
                if (videoData == null) {
                    // The Shorts litho video id filter did not detect the video id.
                    // This is normal in incognito mode, but otherwise is abnormal.
                    Logger.printDebug(() -> "Cannot modify Shorts litho span, data is null");
                    return original;
                }
                // Use the correct dislikes data after voting.
                if (lithoShortsShouldUseCurrentData) {
                    lithoShortsShouldUseCurrentData = false;
                    videoData = currentVideoData;
                    if (videoData == null) {
                        Logger.printException(() -> "currentVideoData is null"); // Should never happen
                        return original;
                    }
                    Logger.printDebug(() -> "Using current video data for litho span");
                }
                replacement = videoData.getDislikeSpanForShort((Spanned) original);
            } else {
                return original;
            }

            return replacement;
        } catch (Exception ex) {
            Logger.printException(() -> "onLithoTextLoaded failure", ex);
        }
        return original;
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
            if (!replacement.toString().equals(original)) {
                rollingNumberSpan = replacement;
                return replacement.toString();
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
            if (Settings.RYD_ENABLED.get() && !Settings.RYD_COMPACT_LAYOUT.get()) {
                if (ReturnYouTubeDislike.isPreviouslyCreatedSegmentedSpan(text)) {
                    // +1 pixel is needed for some foreign languages that measure
                    // the text different from what is used for layout (Greek in particular).
                    // Probably a bug in Android, but who knows.
                    // Single line mode is also used as an additional fix for this issue.
                    return measuredTextWidth + ReturnYouTubeDislike.leftSeparatorBounds.right
                            + ReturnYouTubeDislike.leftSeparatorShapePaddingPixels + 1;
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
            if (Utils.isRightToLeftTextLayout()) {
                view.setCompoundDrawables(null, null, separator, null);
            } else {
                view.setCompoundDrawables(separator, null, null, null);
            }
            // Liking/disliking can cause the span to grow in size,
            // which is ok and is laid out correctly,
            // but if the user then undoes their action the layout will not remove the extra padding.
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
    // Non litho Shorts player.
    //

    /**
     * Replacement text to use for "Dislikes" while RYD is fetching.
     */
    private static final Spannable SHORTS_LOADING_SPAN = new SpannableString("-");

    /**
     * Dislikes TextViews used by Shorts.
     *
     * Multiple TextViews are loaded at once (for the prior and next videos to swipe to).
     * Keep track of all of them, and later pick out the correct one based on their on screen position.
     */
    private static final List<WeakReference<TextView>> shortsTextViewRefs = new ArrayList<>();

    private static void clearRemovedShortsTextViews() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // YouTube requires Android N or greater
            shortsTextViewRefs.removeIf(ref -> ref.get() == null);
        }
    }

    /**
     * Injection point.  Called when a Shorts dislike is updated.  Always on main thread.
     * Handles update asynchronously, otherwise Shorts video will be frozen while the UI thread is blocked.
     *
     * @return if RYD is enabled and the TextView was updated.
     */
    public static boolean setShortsDislikes(@NonNull View likeDislikeView) {
        try {
            if (!Settings.RYD_ENABLED.get()) {
                return false;
            }
            if (!Settings.RYD_SHORTS.get()) {
                // Must clear the data here, in case a new video was loaded while PlayerType
                // suggested the video was not a short (can happen when spoofing to an old app version).
                clearData();
                return false;
            }
            Logger.printDebug(() -> "setShortsDislikes");

            TextView textView = (TextView) likeDislikeView;
            textView.setText(SHORTS_LOADING_SPAN); // Change 'Dislike' text to the loading text.
            shortsTextViewRefs.add(new WeakReference<>(textView));

            if (likeDislikeView.isSelected() && isShortTextViewOnScreen(textView)) {
                Logger.printDebug(() -> "Shorts dislike is already selected");
                ReturnYouTubeDislike videoData = currentVideoData;
                if (videoData != null) videoData.setUserVote(Vote.DISLIKE);
            }

            // For the first short played, the Shorts dislike hook is called after the video id hook.
            // But for most other times this hook is called before the video id (which is not ideal).
            // Must update the TextViews here, and also after the videoId changes.
            updateOnScreenShortsTextViews(false);

            return true;
        } catch (Exception ex) {
            Logger.printException(() -> "setShortsDislikes failure", ex);
            return false;
        }
    }

    /**
     * @param forceUpdate if false, then only update the 'loading text views.
     *                    If true, update all on screen text views.
     */
    private static void updateOnScreenShortsTextViews(boolean forceUpdate) {
        try {
            clearRemovedShortsTextViews();
            if (shortsTextViewRefs.isEmpty()) {
                return;
            }
            ReturnYouTubeDislike videoData = currentVideoData;
            if (videoData == null) {
                return;
            }

            Logger.printDebug(() -> "updateShortsTextViews");

            Runnable update = () -> {
                Spanned shortsDislikesSpan = videoData.getDislikeSpanForShort(SHORTS_LOADING_SPAN);
                Utils.runOnMainThreadNowOrLater(() -> {
                    String videoId = videoData.getVideoId();
                    if (!videoId.equals(VideoInformation.getVideoId())) {
                        // User swiped to new video before fetch completed
                        Logger.printDebug(() -> "Ignoring stale dislikes data for short: " + videoId);
                        return;
                    }

                    // Update text views that appear to be visible on screen.
                    // Only 1 will be the actual textview for the current Short,
                    // but discarded and not yet garbage collected views can remain.
                    // So must set the dislike span on all views that match.
                    for (WeakReference<TextView> textViewRef : shortsTextViewRefs) {
                        TextView textView = textViewRef.get();
                        if (textView == null) {
                            continue;
                        }
                        if (isShortTextViewOnScreen(textView)
                                && (forceUpdate || textView.getText().toString().equals(SHORTS_LOADING_SPAN.toString()))) {
                            Logger.printDebug(() -> "Setting Shorts TextView to: " + shortsDislikesSpan);
                            textView.setText(shortsDislikesSpan);
                        }
                    }
                });
            };
            if (videoData.fetchCompleted()) {
                update.run(); // Network call is completed, no need to wait on background thread.
            } else {
                Utils.runOnBackgroundThread(update);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "updateOnScreenShortsTextViews failure", ex);
        }
    }

    /**
     * Check if a view is within the screen bounds.
     */
    private static boolean isShortTextViewOnScreen(@NonNull View view) {
        final int[] location = new int[2];
        view.getLocationInWindow(location);
        if (location[0] <= 0 && location[1] <= 0) { // Lower bound
            return false;
        }
        Rect windowRect = new Rect();
        view.getWindowVisibleDisplayFrame(windowRect); // Upper bound
        return location[0] < windowRect.width() && location[1] < windowRect.height();
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

            final boolean videoIdIsShort = VideoInformation.lastPlayerResponseIsShort();
            // Shorts shelf in home and subscription feed causes player response hook to be called,
            // and the 'is opening/playing' parameter will be false.
            // This hook will be called again when the Short is actually opened.
            if (videoIdIsShort && (!isShortAndOpeningOrPlaying || !Settings.RYD_SHORTS.get())) {
                return;
            }
            final boolean waitForFetchToComplete = !IS_SPOOFING_TO_NON_LITHO_SHORTS_PLAYER
                    && videoIdIsShort && !lastPlayerResponseWasShort;

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

            ReturnYouTubeDislike data = ReturnYouTubeDislike.getFetchForVideoId(videoId);
            // Pre-emptively set the data to short status.
            // Required to prevent Shorts data from being used on a minimized video in incognito mode.
            if (isNoneHiddenOrSlidingMinimized) {
                data.setVideoIdIsShort(true);
            }
            currentVideoData = data;

            // Current video id hook can be called out of order with the non litho Shorts text view hook.
            // Must manually update again here.
            if (isNoneHiddenOrSlidingMinimized) {
                updateOnScreenShortsTextViews(true);
            }
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
        lithoShortsShouldUseCurrentData = false;
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
     * @param vote int that matches {@link ReturnYouTubeDislike.Vote#value}
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

                    if (isNoneHiddenOrMinimized) {
                        if (lastLithoShortsVideoData != null) {
                            lithoShortsShouldUseCurrentData = true;
                        }
                        updateOldUIDislikesTextView();
                    }

                    return;
                }
            }
            Logger.printException(() -> "Unknown vote type: " + vote);
        } catch (Exception ex) {
            Logger.printException(() -> "sendVote failure", ex);
        }
    }
}
