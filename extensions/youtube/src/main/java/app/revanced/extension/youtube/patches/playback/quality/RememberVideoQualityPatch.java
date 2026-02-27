package app.revanced.extension.youtube.patches.playback.quality;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.NetworkType;

import com.google.android.libraries.youtube.innertube.model.media.VideoQuality;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.settings.IntegerSetting;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.ShortsPlayerState;

@SuppressWarnings("unused")
public class RememberVideoQualityPatch {

    private static final IntegerSetting videoQualityWifi = Settings.VIDEO_QUALITY_DEFAULT_WIFI;
    private static final IntegerSetting videoQualityMobile = Settings.VIDEO_QUALITY_DEFAULT_MOBILE;
    private static final IntegerSetting shortsQualityWifi = Settings.SHORTS_QUALITY_DEFAULT_WIFI;
    private static final IntegerSetting shortsQualityMobile = Settings.SHORTS_QUALITY_DEFAULT_MOBILE;

    public static boolean shouldRememberVideoQuality() {
        BooleanSetting preference = ShortsPlayerState.isOpen()
                ? Settings.REMEMBER_SHORTS_QUALITY_LAST_SELECTED
                : Settings.REMEMBER_VIDEO_QUALITY_LAST_SELECTED;
        return preference.get();
    }

    public static int getDefaultQualityResolution() {
        final boolean isShorts = ShortsPlayerState.isOpen();
        IntegerSetting preference = Utils.getNetworkType() == NetworkType.MOBILE
                ? (isShorts ? shortsQualityMobile : videoQualityMobile)
                : (isShorts ? shortsQualityWifi : videoQualityWifi);
        return preference.get();
    }

    public static void saveDefaultQuality(int qualityResolution) {
        final boolean shortPlayerOpen = ShortsPlayerState.isOpen();
        String networkTypeMessage;
        IntegerSetting qualitySetting;
        if (Utils.getNetworkType() == NetworkType.MOBILE) {
            networkTypeMessage = str("revanced_remember_video_quality_mobile");
            qualitySetting = shortPlayerOpen ? shortsQualityMobile : videoQualityMobile;
        } else {
            networkTypeMessage = str("revanced_remember_video_quality_wifi");
            qualitySetting = shortPlayerOpen ? shortsQualityWifi : videoQualityWifi;
        }

        if (qualitySetting.get() == qualityResolution) {
            // User clicked the same video quality as the current video,
            // or changed between 1080p Premium and non-Premium.
            return;
        }
        qualitySetting.save(qualityResolution);

        if (Settings.REMEMBER_VIDEO_QUALITY_LAST_SELECTED_TOAST.get()) {
            String qualityLabel = qualityResolution + "p";
            Utils.showToastShort(str(
                    shortPlayerOpen
                            ? "revanced_remember_video_quality_toast_shorts"
                            : "revanced_remember_video_quality_toast",
                    networkTypeMessage,
                    qualityLabel)
            );
        }
    }

    /**
     * Injection point.
     * @param userSelectedQualityIndex Element index of {@link VideoInformation#getCurrentQualities()}.
     */
    public static void userChangedShortsQuality(int userSelectedQualityIndex) {
        try {
            if (shouldRememberVideoQuality()) {
                VideoQuality[] currentQualities = VideoInformation.getCurrentQualities();
                if (currentQualities == null) {
                    Logger.printDebug(() -> "Cannot save default quality, qualities is null");
                    return;
                }
                VideoQuality quality = currentQualities[userSelectedQualityIndex];
                saveDefaultQuality(quality.patch_getResolution());
            }
        } catch (Exception ex) {
            Logger.printException(() -> "userChangedShortsQuality failure", ex);
        }
    }

    /**
     * Injection point.  Regular videos.
     * @param videoResolution Human readable resolution: 480, 720, 1080.
     */
    public static void userChangedQuality(int videoResolution) {
        Utils.verifyOnMainThread();
        Logger.printDebug(() -> "User changed quality to: " + videoResolution);

        if (shouldRememberVideoQuality()) {
            saveDefaultQuality(videoResolution);
        }
    }

    /**
     * Injection point.
     */
    public static void newVideoStarted(VideoInformation.PlaybackController ignoredPlayerController) {
        VideoInformation.setDesiredVideoResolution(getDefaultQualityResolution());
    }
}
