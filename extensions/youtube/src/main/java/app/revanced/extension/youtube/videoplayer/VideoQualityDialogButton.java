package app.revanced.extension.youtube.videoplayer;

import android.view.View;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.patches.playback.quality.RememberVideoQualityPatch;
import app.revanced.extension.youtube.settings.Settings;

import java.util.List;

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
            // Get the current quality.
            int lastAppliedQualityIndex = RememberVideoQualityPatch.getLastAppliedQualityIndex();
            List<Integer> videoQualities = RememberVideoQualityPatch.getVideoQualities();

            if (videoQualities == null || lastAppliedQualityIndex < 0 || lastAppliedQualityIndex >= videoQualities.size()) {
                // Default to a generic icon or "Auto".
                instance.setIcon("revanced_video_quality_dialog_button");
                return;
            }

            int quality = videoQualities.get(lastAppliedQualityIndex);
            String iconResource = switch (quality) {
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
                            updateButtonIcon(); // Update icon after dialog interaction
                        } catch (Exception ex) {
                            Logger.printException(() -> "Video quality button onClick failure", ex);
                        }
                    },
                    view -> {
                        try {
                            // Reset to automatic quality.
                            final int autoQuality = -2; // Auto.
                            RememberVideoQualityPatch.userChangedQualityInNewFlyout(autoQuality);
                            updateButtonIcon(); // Update icon after reset.
                            showToastShort(str("revanced_video_quality_reset_toast"));
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

