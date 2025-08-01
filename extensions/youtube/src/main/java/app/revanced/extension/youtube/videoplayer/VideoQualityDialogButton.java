package app.revanced.extension.youtube.videoplayer;

import android.view.View;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.patches.playback.quality.RememberVideoQualityPatch;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.ShortsPlayerState;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.showToastShort;

@SuppressWarnings("unused")
public class VideoQualityDialogButton {
    @Nullable
    private static PlayerControlButton instance;

    /**
     * Updates the button icon based on the current video quality.
     */
    public static void updateButtonIcon() {
        if (instance == null) return;

        try {
            // Get the current preferred quality from settings based on network and player state.
            boolean isShorts = ShortsPlayerState.isOpen();
            int currentQuality = Utils.getNetworkType() == Utils.NetworkType.MOBILE
                    ? (isShorts ? Settings.SHORTS_QUALITY_DEFAULT_MOBILE : Settings.VIDEO_QUALITY_DEFAULT_MOBILE).get()
                    : (isShorts ? Settings.SHORTS_QUALITY_DEFAULT_WIFI : Settings.VIDEO_QUALITY_DEFAULT_WIFI).get();

            // Map quality to appropriate icon.
            String iconResource = switch (currentQuality) {
                case 144, 240, 360, 480 -> "revanced_video_quality_dialog_button_lhd";
                case 720  -> "revanced_video_quality_dialog_button_hd";
                case 1080 -> "revanced_video_quality_dialog_button_fhd";
                case 1440 -> "revanced_video_quality_dialog_button_2k";
                case 2160 -> "revanced_video_quality_dialog_button_4k";
                default   -> "revanced_video_quality_dialog_button";
            };

            instance.setIcon(iconResource);
            Logger.printDebug(() -> "Updated button icon to: " + iconResource);
        } catch (Exception ex) {
            Logger.printException(() -> "Failed to update button icon", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void initializeButton(View controlsView) {
        try {
            instance = new PlayerControlButton(
                    controlsView,
                    "revanced_video_quality_dialog_button",
                    "revanced_video_quality_dialog_button_placeholder",
                    Settings.VIDEO_QUALITY_DIALOG_BUTTON::get,
                    view -> {
                        try {
                            RememberVideoQualityPatch.showVideoQualityDialog(view.getContext());
                            updateButtonIcon(); // Update icon after dialog interaction.
                        } catch (Exception ex) {
                            Logger.printException(() -> "Video quality button onClick failure", ex);
                        }
                    },
                    view -> {
                        try {
                            // Reset to automatic quality.
                            final int autoQuality = -2; // Auto.
                            RememberVideoQualityPatch.userChangedQualityInFlyout(autoQuality);
                            // Apply automatic quality immediately.
                            if (RememberVideoQualityPatch.getCurrentMenuInterface() != null && RememberVideoQualityPatch.getVideoQualities() != null) {
                                RememberVideoQualityPatch.getCurrentMenuInterface().patch_setMenuIndexFromQuality(
                                        RememberVideoQualityPatch.getVideoQualities().get(0)); // Auto is index 0.
                                Logger.printDebug(() -> "Applied automatic quality via long press");
                            }
                            updateButtonIcon(); // Update icon after reset.
                            return true;
                        } catch (Exception ex) {
                            Logger.printException(() -> "Video quality button reset failure", ex);
                            return false;
                        }
                    }
            );
            updateButtonIcon(); // Set initial icon.
        } catch (Exception ex) {
            Logger.printException(() -> "initializeButton failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void setVisibilityImmediate(boolean visible) {
        if (instance != null) {
            instance.setVisibilityImmediate(visible);
            if (visible) updateButtonIcon();
        }
    }

    /**
     * Injection point.
     */
    public static void setVisibility(boolean visible, boolean animated) {
        if (instance != null) {
            instance.setVisibility(visible, animated);
            if (visible) updateButtonIcon();
        }
    }
}
