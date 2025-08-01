package app.revanced.extension.youtube.videoplayer;

import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.libraries.youtube.innertube.model.media.VideoQuality;

import java.util.List;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.patches.playback.quality.RememberVideoQualityPatch;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class VideoQualityDialogButton {
    @Nullable
    private static PlayerControlButton instance;

    /**
     * The current resource name of the button icon.
     */
    @Nullable
    private static String currentIconResource;

    /**
     * Updates the button icon based on the current video quality.
     */
    public static void updateButtonIcon() {
        try {
            if (instance == null) return;

            VideoQuality currentQuality = RememberVideoQualityPatch.getCurrentQuality();
            final int resolution = currentQuality == null
                    ? RememberVideoQualityPatch.AUTOMATIC_VIDEO_QUALITY_VALUE
                    : currentQuality.patch_getResolution();

            // Map quality to appropriate icon.
            String iconResource = switch (resolution) {
                case 144, 240, 360, 480 -> "revanced_video_quality_dialog_button_lhd";
                case 720  -> "revanced_video_quality_dialog_button_hd";
                case 1080 -> "revanced_video_quality_dialog_button_fhd";
                case 1440 -> "revanced_video_quality_dialog_button_qhd";
                case 2160 -> "revanced_video_quality_dialog_button_4k";
                default   -> "revanced_video_quality_dialog_button";
            };

            if (!iconResource.equals(currentIconResource)) {
                currentIconResource = iconResource;
                instance.setIcon(iconResource);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "updateButtonIcon failure", ex);
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
                            RememberVideoQualityPatch.userChangedQualityInFlyout(
                                    RememberVideoQualityPatch.AUTOMATIC_VIDEO_QUALITY_VALUE);

                            // Apply automatic quality immediately.
                            List<VideoQuality> qualities = RememberVideoQualityPatch.getCurrentQualities();
                            RememberVideoQualityPatch.VideoQualityMenuInterface menu
                                    = RememberVideoQualityPatch.getCurrentMenuInterface();
                            if (qualities != null && menu != null) {
                                menu.patch_setMenuIndexFromQuality(qualities.get(0)); // Auto is index 0.
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
