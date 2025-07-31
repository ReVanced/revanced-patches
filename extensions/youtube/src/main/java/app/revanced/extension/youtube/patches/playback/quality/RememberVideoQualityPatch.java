package app.revanced.extension.youtube.patches.playback.quality;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.NetworkType;

import androidx.annotation.Nullable;

import com.google.android.libraries.youtube.innertube.model.media.VideoQuality;

import java.util.Arrays;
import java.util.List;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.settings.IntegerSetting;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.ShortsPlayerState;

@SuppressWarnings("unused")
public class RememberVideoQualityPatch {

    /**
     * Interface to use obfuscated methods.
     */
    public interface VideoQualityMenuInterface {
        void patch_setMenuIndexFromQuality(VideoQuality quality);
    }

    private static final int AUTOMATIC_VIDEO_QUALITY_VALUE = -2;
    private static final IntegerSetting videoQualityWifi = Settings.VIDEO_QUALITY_DEFAULT_WIFI;
    private static final IntegerSetting videoQualityMobile = Settings.VIDEO_QUALITY_DEFAULT_MOBILE;
    private static final IntegerSetting shortsQualityWifi = Settings.SHORTS_QUALITY_DEFAULT_WIFI;
    private static final IntegerSetting shortsQualityMobile = Settings.SHORTS_QUALITY_DEFAULT_MOBILE;

    private static boolean qualityNeedsUpdating;

    /**
     * If the user selected a new quality from the flyout menu,
     * and {@link Settings#REMEMBER_VIDEO_QUALITY_LAST_SELECTED}
     * or {@link Settings#REMEMBER_SHORTS_QUALITY_LAST_SELECTED} is enabled.
     */
    private static boolean userChangedDefaultQuality;

    /**
     * Index of the video quality chosen by the user from the flyout menu.
     */
    private static int userSelectedQualityIndex;

    /**
     * The available qualities of the current video.
     */
    @Nullable
    private static List<VideoQuality> videoQualities;

    private static boolean shouldRememberVideoQuality() {
        BooleanSetting preference = ShortsPlayerState.isOpen() ?
                Settings.REMEMBER_SHORTS_QUALITY_LAST_SELECTED
                : Settings.REMEMBER_VIDEO_QUALITY_LAST_SELECTED;
        return preference.get();
    }

    private static void changeDefaultQuality(int qualityResolution) {
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
        qualitySetting.save(qualityResolution);

        if (Settings.REMEMBER_VIDEO_QUALITY_LAST_SELECTED_TOAST.get())
            Utils.showToastShort(str(
                    shortPlayerOpen
                            ? "revanced_remember_video_quality_toast_shorts"
                            : "revanced_remember_video_quality_toast",
                    networkTypeMessage,
                    (qualityResolution + "p"))
            );
    }

    /**
     * Injection point.
     *
     * @param qualities Video qualities available, ordered from largest to smallest, with index 0 being the 'automatic' value of -2
     * @param originalQualityIndex quality index to use, as chosen by YouTube
     */
    public static int setVideoQuality(VideoQuality[] qualities, VideoQualityMenuInterface menu, int originalQualityIndex) {
        try {
            Utils.verifyOnMainThread();

            final boolean useShortsPreference = ShortsPlayerState.isOpen();
            final int preferredQuality = Utils.getNetworkType() == NetworkType.MOBILE
                    ? (useShortsPreference ? shortsQualityMobile : videoQualityMobile).get()
                    : (useShortsPreference ? shortsQualityWifi : videoQualityWifi).get();

            if (!userChangedDefaultQuality && preferredQuality == AUTOMATIC_VIDEO_QUALITY_VALUE) {
                return originalQualityIndex; // Nothing to do.
            }

            if (videoQualities == null || videoQualities.size() != qualities.length) {
                videoQualities = Arrays.asList(qualities);

                // After changing videos the qualities can initially be for the prior video.
                // So if the qualities have changed an update is needed.
                qualityNeedsUpdating = true;
                Logger.printDebug(() -> "VideoQualities: " + videoQualities);
            }

            if (userChangedDefaultQuality) {
                userChangedDefaultQuality = false;
                VideoQuality quality = videoQualities.get(userSelectedQualityIndex);
                Logger.printDebug(() -> "User changed default quality to: " + quality);
                changeDefaultQuality(quality.patch_getResolution());
                return userSelectedQualityIndex;
            }

            if (!qualityNeedsUpdating) {
                return originalQualityIndex;
            }
            qualityNeedsUpdating = false;

            // Find the highest quality that is equal to or less than the preferred.
            VideoQuality qualityToUse = videoQualities.get(0); // First element is automatic mode.
            int qualityIndexToUse = 0;
            int i = 0;
            for (VideoQuality quality : videoQualities) {
                final int qualityResolution = quality.patch_getResolution();
                if (qualityResolution > qualityToUse.patch_getResolution() && qualityResolution <= preferredQuality) {
                    qualityToUse = quality;
                    qualityIndexToUse = i;
                    break;
                }
                i++;
            }

            // If the desired quality index is equal to the original index,
            // then the video is already set to the desired default quality.
            String qualityToUseName = qualityToUse.patch_getQualityName();
            if (qualityIndexToUse == originalQualityIndex) {
                Logger.printDebug(() -> "Video is already preferred quality: " + qualityToUseName);
            } else {
                Logger.printDebug(() -> "Changing video quality from: "
                        + videoQualities.get(originalQualityIndex).patch_getQualityName()
                        + " to: " + qualityToUseName);
            }

            // On first load of a new video, if the video is already the desired quality
            // then the quality flyout will show 'Auto' (ie: Auto (720p)).
            //
            // To prevent user confusion, set the video index even if the
            // quality is already correct so the UI picker will not display "Auto".
            menu.patch_setMenuIndexFromQuality(qualities[qualityIndexToUse]);

            return qualityIndexToUse;
        } catch (Exception ex) {
            Logger.printException(() -> "setVideoQuality failure", ex);
            return originalQualityIndex;
        }
    }

    /**
     * Injection point. Fixes bad data used by YouTube.
     */
    public static int fixVideoQualityResolution(String name, int quality) {
        final int correctQuality = 480;
        if (name.equals("480p") && quality != correctQuality) {
            Logger.printDebug(() -> "Fixing bad data of " + name + " from: " + quality
                    + " to: " + correctQuality);
            return correctQuality;
        }

        return quality;
    }

    /**
     * Injection point.
     * @param qualityIndex Element index of {@link #videoQualities}.
     */
    public static void userChangedQuality(int qualityIndex) {
        if (shouldRememberVideoQuality()) {
            userSelectedQualityIndex = qualityIndex;
            userChangedDefaultQuality = true;
        }
    }

    /**
     * Injection point.
     * @param videoResolution Human readable resolution: 480, 720, 1080.
     */
    public static void userChangedQualityInFlyout(int videoResolution) {
        Utils.verifyOnMainThread();
        if (!shouldRememberVideoQuality()) return;

        changeDefaultQuality(videoResolution);
    }

    /**
     * Injection point.
     */
    public static void newVideoStarted(VideoInformation.PlaybackController ignoredPlayerController) {
        Utils.verifyOnMainThread();

        Logger.printDebug(() -> "newVideoStarted");
        qualityNeedsUpdating = true;
        videoQualities = null;
    }
}
