package app.revanced.extension.youtube.sponsorblock.ui;

import static app.revanced.extension.shared.Utils.getResourceIdentifier;
import static app.revanced.extension.shared.Utils.getResourceIdentifierOrThrow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.ResourceType;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.PlayerType;
import app.revanced.extension.youtube.sponsorblock.SegmentPlaybackController;
import app.revanced.extension.youtube.sponsorblock.objects.SponsorSegment;
import app.revanced.extension.youtube.videoplayer.PlayerControlButton;
import kotlin.Unit;

public class SponsorBlockViewController {
    public static final int ROUNDED_LAYOUT_MARGIN = 12;

    private static WeakReference<RelativeLayout> inlineSponsorOverlayRef = new WeakReference<>(null);
    private static WeakReference<ViewGroup> youtubeOverlaysLayoutRef = new WeakReference<>(null);
    private static WeakReference<SkipSponsorButton> skipHighlightButtonRef = new WeakReference<>(null);
    private static WeakReference<NewSegmentLayout> newSegmentLayoutRef = new WeakReference<>(null);
    private static WeakReference<SkipSponsorButton> skipSponsorButtonRef = new WeakReference<>(null);
    @Nullable
    private static PlayerControlButton skipSponsorPlayerButton;
    private static boolean canShowViewElements;
    private static boolean newSegmentLayoutVisible;
    @Nullable
    private static SponsorSegment skipHighlight;
    @Nullable
    private static SponsorSegment skipSegment;

    static {
        PlayerType.getOnChange().addObserver((PlayerType type) -> {
            playerTypeChanged(type);
            return Unit.INSTANCE;
        });
    }

    public static Context getOverLaysViewGroupContext() {
        ViewGroup group = youtubeOverlaysLayoutRef.get();
        if (group == null) {
            return null;
        }
        return group.getContext();
    }

    /**
     * Injection point.
     */
    public static void initialize(ViewGroup viewGroup) {
        try {
            Logger.printDebug(() -> "initializing");

            // hide any old components, just in case they somehow are still hanging around
            hideAll();

            Context context = Utils.getContext();
            RelativeLayout layout = new RelativeLayout(context);
            layout.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            LayoutInflater.from(context).inflate(getResourceIdentifierOrThrow(
                    ResourceType.LAYOUT, "revanced_sb_inline_sponsor_overlay"), layout);
            inlineSponsorOverlayRef = new WeakReference<>(layout);

            viewGroup.addView(layout);
            viewGroup.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                @Override
                public void onChildViewAdded(View parent, View child) {
                    // Ensure SB buttons and controls are always on top, otherwise the end-screen cards can cover the skip button.
                    RelativeLayout layout = inlineSponsorOverlayRef.get();
                    if (layout != null) {
                        layout.bringToFront();
                    }
                }
                @Override
                public void onChildViewRemoved(View parent, View child) {
                }
            });
            youtubeOverlaysLayoutRef = new WeakReference<>(viewGroup);

            skipHighlightButtonRef = new WeakReference<>(Objects.requireNonNull(layout.findViewById(
                    getResourceIdentifier(ResourceType.ID, "revanced_sb_skip_highlight_button"))));

            skipSponsorButtonRef = new WeakReference<>(Objects.requireNonNull(layout.findViewById(
                    getResourceIdentifier(ResourceType.ID, "revanced_sb_skip_sponsor_button"))));

            // Handles fading in/out with the player overlay.
            skipSponsorPlayerButton = new PlayerControlButton(
                    layout,
                    "revanced_sb_skip_sponsor_button",
                    null,
                    () -> canShowViewElements && SegmentPlaybackController.currentlyInsideSkippableSegment(),
                    view -> {
                        SkipSponsorButton button = skipSponsorButtonRef.get();
                        if (button != null) {
                            button.skipButtonClicked();
                        }
                    },
                    null
            );

            NewSegmentLayout newSegmentLayout = Objects.requireNonNull(layout.findViewById(
                    getResourceIdentifier(ResourceType.ID, "revanced_sb_new_segment_view")));
            newSegmentLayoutRef = new WeakReference<>(newSegmentLayout);
            newSegmentLayout.updateLayout();

            newSegmentLayoutVisible = false;
            skipHighlight = null;
            skipSegment = null;
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    public static void hideAll() {
        hideSkipHighlightButton();
        hideSkipSegmentButton();
        hideNewSegmentLayout();
    }

    public static void updateLayout() {
        SkipSponsorButton button = skipSponsorButtonRef.get();
        if (button != null) {
            button.updateLayout();
        }

        button = skipHighlightButtonRef.get();
        if (button != null) {
            button.updateLayout();
        }

        NewSegmentLayout newSegmentLayout = newSegmentLayoutRef.get();
        if (newSegmentLayout != null) {
            newSegmentLayout.updateLayout();
        }
    }

    public static void showSkipHighlightButton(SponsorSegment segment) {
        skipHighlight = Objects.requireNonNull(segment);
        NewSegmentLayout newSegmentLayout = newSegmentLayoutRef.get();
        // Don't show highlight button if create new segment is visible.
        final boolean buttonVisibility = newSegmentLayout == null || newSegmentLayout.getVisibility() != View.VISIBLE;
        updateSkipButton(skipHighlightButtonRef.get(), false, segment, buttonVisibility);
    }

    /**
     * Same as {@link #setSkipSegment(SponsorSegment)} and it forcefully shows the skip button.
     */
    public static void showSkipSegmentButton(SponsorSegment segment) {
        skipSegment = Objects.requireNonNull(segment);
        updateSkipButton(skipSponsorButtonRef.get(), true, segment, true);
    }

    /**
     * Sets the skip segment and updates the button, but does not forcefully show the skip button.
     */
    public static void setSkipSegment(SponsorSegment segment) {
        skipSegment = Objects.requireNonNull(segment);
        SkipSponsorButton button = skipSponsorButtonRef.get();
        if (button != null) {
            button.updateSkipButtonText(segment);
        }
    }

    public static void hideSkipHighlightButton() {
        skipHighlight = null;
        updateSkipButton(skipHighlightButtonRef.get(), false, null, false);
    }

    public static void hideSkipSegmentButton() {
        if (!Settings.SB_AUTO_HIDE_SKIP_BUTTON.get()) {
            // Must retain segment for auto hide because skip button is shown when player overlay is active.
            skipSegment = null;
        }
        updateSkipButton(skipSponsorButtonRef.get(), true, null, false);
    }

    private static void updateSkipButton(@Nullable SkipSponsorButton button,
                                         boolean isSkipButton,
                                         @Nullable SponsorSegment segment,
                                         boolean visible) {
        if (button == null) {
            return;
        }
        if (segment != null) {
            button.updateSkipButtonText(segment);
        }

        if (isSkipButton && Settings.SB_AUTO_HIDE_SKIP_BUTTON.get()) {
            setVisibilityImmediate(visible);
        } else {
            setGenericViewVisibility(button, visible);
        }
    }

    public static void toggleNewSegmentLayoutVisibility() {
        NewSegmentLayout newSegmentLayout = newSegmentLayoutRef.get();
        if (newSegmentLayout == null) { // should never happen
            Logger.printException(() -> "toggleNewSegmentLayoutVisibility failure");
            return;
        }
        newSegmentLayoutVisible = (newSegmentLayout.getVisibility() != View.VISIBLE);
        if (skipHighlight != null) {
            setGenericViewVisibility(skipHighlightButtonRef.get(), !newSegmentLayoutVisible);
        }
        setGenericViewVisibility(newSegmentLayout, newSegmentLayoutVisible);
    }

    public static void hideNewSegmentLayout() {
        newSegmentLayoutVisible = false;
        setGenericViewVisibility(newSegmentLayoutRef.get(), false);
    }

    private static void setGenericViewVisibility(@Nullable View view, boolean visible) {
        if (view == null) {
            return;
        }

        visible &= canShowViewElements;
        final int desiredVisibility = visible ? View.VISIBLE : View.GONE;
        if (view.getVisibility() != desiredVisibility) {
            view.setVisibility(desiredVisibility);
        }
    }

    private static void playerTypeChanged(PlayerType playerType) {
        try {
            final boolean isWatchFullScreen = playerType == PlayerType.WATCH_WHILE_FULLSCREEN;
            canShowViewElements = (isWatchFullScreen || playerType == PlayerType.WATCH_WHILE_MAXIMIZED);

            NewSegmentLayout newSegmentLayout = newSegmentLayoutRef.get();
            setNewSegmentLayoutMargins(newSegmentLayout, isWatchFullScreen);
            setGenericViewVisibility(newSegmentLayoutRef.get(), newSegmentLayoutVisible);

            SkipSponsorButton skipHighlightButton = skipHighlightButtonRef.get();
            setSkipButtonMargins(skipHighlightButton, isWatchFullScreen);
            setGenericViewVisibility(skipHighlightButton, skipHighlight != null);

            SkipSponsorButton skipSponsorButton = skipSponsorButtonRef.get();
            setSkipButtonMargins(skipSponsorButton, isWatchFullScreen);
            setGenericViewVisibility(skipSponsorButton, skipSegment != null);
        } catch (Exception ex) {
            Logger.printException(() -> "playerTypeChanged failure", ex);
        }
    }

    private static void setNewSegmentLayoutMargins(@Nullable NewSegmentLayout layout, boolean fullScreen) {
         if (layout != null) {
            setLayoutMargins(layout, fullScreen, layout.defaultBottomMargin, layout.ctaBottomMargin);
        }
    }
    private static void setSkipButtonMargins(@Nullable SkipSponsorButton button, boolean fullScreen) {
        if (button != null) {
            setLayoutMargins(button, fullScreen, button.defaultBottomMargin, button.ctaBottomMargin);
        }
    }
    private static void setLayoutMargins(View view, boolean fullScreen,
                                         int defaultBottomMargin, int ctaBottomMargin) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if (params == null) {
            Logger.printException(() -> "Unable to setNewSegmentLayoutMargins (params are null)");
            return;
        }
        params.bottomMargin = fullScreen ? ctaBottomMargin : defaultBottomMargin;
        view.setLayoutParams(params);
    }

    // Additional logic to fade in/out the skip segment button when autohide skip button is active.

    /**`
     * injection point.
     */
    public static void setVisibilityNegatedImmediate() {
        if (SegmentPlaybackController.shouldNotFadeOutPlayerOverlaySkipButton()) {
            return;
        }

        if (skipSponsorPlayerButton != null) {
            skipSponsorPlayerButton.setVisibilityNegatedImmediate();
        }
    }

    /**
     * injection point.
     * Only for skip button when auto hide is enbled.
     */
    public static void setVisibilityImmediate(boolean visible) {
        if (!visible && SegmentPlaybackController.shouldNotFadeOutPlayerOverlaySkipButton()) {
            return;
        }

        if (skipSponsorPlayerButton != null) {
            skipSponsorPlayerButton.setVisibilityImmediate(visible);
        }
    }

    /**
     * injection point.
     */
    public static void setVisibility(boolean visible, boolean animated) {
        if (!visible && SegmentPlaybackController.shouldNotFadeOutPlayerOverlaySkipButton()) {
            return;
        }

        if (skipSponsorPlayerButton != null) {
            skipSponsorPlayerButton.setVisibility(visible, animated);
        }
    }
}
