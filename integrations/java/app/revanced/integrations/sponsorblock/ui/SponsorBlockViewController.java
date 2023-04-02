package app.revanced.integrations.sponsorblock.ui;

import static app.revanced.integrations.utils.ReVancedUtils.getResourceIdentifier;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.sponsorblock.objects.SponsorSegment;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class SponsorBlockViewController {
    private static WeakReference<RelativeLayout> inlineSponsorOverlayRef = new WeakReference<>(null);
    private static WeakReference<ViewGroup> youtubeOverlaysLayoutRef = new WeakReference<>(null);
    private static WeakReference<SkipSponsorButton> skipSponsorButtonRef = new WeakReference<>(null);
    private static WeakReference<NewSegmentLayout> newSegmentLayoutRef = new WeakReference<>(null);
    private static boolean canShowViewElements = true;
    @Nullable
    private static SponsorSegment skipSegment;

    static {
        PlayerType.getOnChange().addObserver((PlayerType type) -> {
            playerTypeChanged(type);
            return null;
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
    public static void initialize(Object obj) {
        try {
            LogHelper.printDebug(() -> "initializing");

            RelativeLayout layout = new RelativeLayout(ReVancedUtils.getContext());
            layout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
            LayoutInflater.from(ReVancedUtils.getContext()).inflate(getResourceIdentifier("inline_sponsor_overlay", "layout"), layout);
            inlineSponsorOverlayRef = new WeakReference<>(layout);

            ViewGroup viewGroup = (ViewGroup) obj;
            viewGroup.addView(layout, viewGroup.getChildCount() - 2);
            youtubeOverlaysLayoutRef = new WeakReference<>(viewGroup);

            skipSponsorButtonRef = new WeakReference<>(
                    Objects.requireNonNull(layout.findViewById(getResourceIdentifier("sb_skip_sponsor_button", "id"))));

            newSegmentLayoutRef = new WeakReference<>(
                    Objects.requireNonNull(layout.findViewById(getResourceIdentifier("sb_new_segment_view", "id"))));
        } catch (Exception ex) {
            LogHelper.printException(() -> "initialize failure", ex);
        }
    }

    public static void showSkipButton(@NonNull SponsorSegment info) {
        skipSegment = Objects.requireNonNull(info);
        updateSkipButton();
    }

    public static void hideSkipButton() {
        skipSegment = null;
        updateSkipButton();
    }

    private static void updateSkipButton() {
        SkipSponsorButton skipSponsorButton = skipSponsorButtonRef.get();
        if (skipSponsorButton == null) {
            return;
        }
        if (skipSegment == null) {
            setSkipSponsorButtonVisibility(false);
        } else {
            final boolean layoutNeedsUpdating = skipSponsorButton.updateSkipButtonText(skipSegment);
            if (layoutNeedsUpdating) {
                bringLayoutToFront();
            }
            setSkipSponsorButtonVisibility(true);
        }
    }

    public static void showNewSegmentLayout() {
        setNewSegmentLayoutVisibility(true);
    }

    public static void hideNewSegmentLayout() {
        NewSegmentLayout newSegmentLayout = newSegmentLayoutRef.get();
        if (newSegmentLayout == null) {
            return;
        }
        setNewSegmentLayoutVisibility(false);
    }

    public static void toggleNewSegmentLayoutVisibility() {
        NewSegmentLayout newSegmentLayout = newSegmentLayoutRef.get();
        if (newSegmentLayout == null) {
            LogHelper.printException(() -> "toggleNewSegmentLayoutVisibility failure");
            return;
        }
        setNewSegmentLayoutVisibility(newSegmentLayout.getVisibility() == View.VISIBLE ? false : true);
    }

    private static void playerTypeChanged(PlayerType playerType) {
        try {
            final boolean isWatchFullScreen = playerType == PlayerType.WATCH_WHILE_FULLSCREEN;
            canShowViewElements = (isWatchFullScreen || playerType == PlayerType.WATCH_WHILE_MAXIMIZED);

            setSkipButtonMargins(isWatchFullScreen);
            setNewSegmentLayoutMargins(isWatchFullScreen);
            updateSkipButton();
        } catch (Exception ex) {
            LogHelper.printException(() -> "Player type changed error", ex);
        }
    }

    private static void setSkipButtonMargins(boolean fullScreen) {
        SkipSponsorButton skipSponsorButton = skipSponsorButtonRef.get();
        if (skipSponsorButton == null) {
            LogHelper.printException(() -> "setSkipButtonMargins failure");
            return;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) skipSponsorButton.getLayoutParams();
        if (params == null) {
            LogHelper.printException(() -> "setSkipButtonMargins failure");
            return;
        }
        params.bottomMargin = fullScreen ? skipSponsorButton.ctaBottomMargin : skipSponsorButton.defaultBottomMargin;
        skipSponsorButton.setLayoutParams(params);
    }

    private static void setSkipSponsorButtonVisibility(boolean visible) {
        SkipSponsorButton skipSponsorButton = skipSponsorButtonRef.get();
        if (skipSponsorButton == null) {
            LogHelper.printException(() -> "setSkipSponsorButtonVisibility failure");
            return;
        }

        visible &= canShowViewElements;

        final int desiredVisibility = visible ? View.VISIBLE : View.GONE;
        if (skipSponsorButton.getVisibility() != desiredVisibility) {
            skipSponsorButton.setVisibility(desiredVisibility);
            if (visible) {
                bringLayoutToFront();
            }
        }
    }

    private static void setNewSegmentLayoutMargins(boolean fullScreen) {
        NewSegmentLayout newSegmentLayout = newSegmentLayoutRef.get();
        if (newSegmentLayout == null) {
            LogHelper.printException(() -> "Unable to setNewSegmentLayoutMargins (button is null)");
            return;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) newSegmentLayout.getLayoutParams();
        if (params == null) {
            LogHelper.printException(() -> "Unable to setNewSegmentLayoutMargins (params are null)");
            return;
        }
        params.bottomMargin = fullScreen ? newSegmentLayout.ctaBottomMargin : newSegmentLayout.defaultBottomMargin;
        newSegmentLayout.setLayoutParams(params);
    }

    private static void setNewSegmentLayoutVisibility(boolean visible) {
        NewSegmentLayout newSegmentLayout = newSegmentLayoutRef.get();
        if (newSegmentLayout == null) {
            LogHelper.printException(() -> "setNewSegmentLayoutVisibility failure");
            return;
        }

        visible &= canShowViewElements;

        final int desiredVisibility = visible ? View.VISIBLE : View.GONE;
        if (newSegmentLayout.getVisibility() != desiredVisibility) {
            newSegmentLayout.setVisibility(desiredVisibility);
            if (visible) {
                bringLayoutToFront();
            }
        }
    }

    private static void bringLayoutToFront() {
        RelativeLayout layout = inlineSponsorOverlayRef.get();
        if (layout != null) {
            // needed to keep skip button overtop end screen cards
            layout.bringToFront();
            layout.requestLayout();
            layout.invalidate();
        }
    }

    /**
     * Injection point.
     */
    public static void endOfVideoReached() {
        try {
            LogHelper.printDebug(() -> "endOfVideoReached");
            // the buttons automatically set themselves to visible when appropriate,
            // but if buttons are showing when the end of the video is reached then they need
            // to be forcefully hidden
            if (!SettingsEnum.PREFERRED_AUTO_REPEAT.getBoolean()) {
                CreateSegmentButtonController.hide();
                VotingButtonController.hide();
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "endOfVideoReached failure", ex);
        }
    }
}
