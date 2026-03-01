package app.revanced.extension.youtube.patches.playback.quality;

import static app.revanced.extension.youtube.patches.VideoInformation.isPremiumVideoQuality;

import java.util.Arrays;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.patches.VideoInformation.VideoQualityInterface;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class HidePremiumVideoQualityPatch {
    private static final boolean HIDE_PREMIUM_VIDEO_QUALITY = Settings.HIDE_PREMIUM_VIDEO_QUALITY.get();

    /**
     * Injection point.
     */
    public static Object[] hidePremiumVideoQuality(VideoQualityInterface[] qualities) {
        if (HIDE_PREMIUM_VIDEO_QUALITY && qualities != null && qualities.length > 0) {
            try {
                return Arrays.stream(qualities)
                        .filter(quality -> quality != null && !isPremiumVideoQuality(quality))
                        .toArray(VideoQualityInterface[]::new);
            } catch (Exception ex) {
                Logger.printException(() -> "Failed to hide Premium video quality", ex);
            }
        }

        return qualities;
    }
}