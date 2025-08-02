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
import app.revanced.extension.youtube.videoplayer.VideoQualityDialogButton;

@SuppressWarnings("unused")
public class RememberVideoQualityPatch {

    /**
     * Interface to use obfuscated methods.
     */
    public interface VideoQualityMenuInterface {
        void patch_setQuality(VideoQuality quality);
    }

    public static final int AUTOMATIC_VIDEO_QUALITY_VALUE = -2;

    /**
     * All video names are the same for all languages, including enhanced bitrate.
     * VideoQuality also has a resolution enum that can be used if needed.
     */
    public static final String VIDEO_QUALITY_1080P_ENHANCED = "1080p Premium";

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
    private static List<VideoQuality> currentQualities;

    /**
     * The current quality of the video playing.  This can never be the automatic value.
     */
    @Nullable
    private static VideoQuality currentQuality;

    /**
     * The current VideoQualityMenuInterface, set during setVideoQuality.
     */
    @Nullable
    private static VideoQualityMenuInterface currentMenuInterface;

    @Nullable
    public static List<VideoQuality> getCurrentQualities() {
        return currentQualities;
    }

    @Nullable
    public static VideoQuality getCurrentQuality() {
        return currentQuality;
    }

    @Nullable
    public static VideoQualityMenuInterface getCurrentMenuInterface() {
        return currentMenuInterface;
    }

    public static boolean shouldRememberVideoQuality() {
        BooleanSetting preference = ShortsPlayerState.isOpen() ?
                Settings.REMEMBER_SHORTS_QUALITY_LAST_SELECTED
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
        qualitySetting.save(qualityResolution);

        if (Settings.REMEMBER_VIDEO_QUALITY_LAST_SELECTED_TOAST.get()) {
            String qualityLabel = qualityResolution == AUTOMATIC_VIDEO_QUALITY_VALUE
                    ? str("video_quality_quick_menu_auto_toast")
                    : qualityResolution + "p";
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
     *
     * @param qualities Video qualities available, ordered from largest to smallest, with index 0 being the 'automatic' value of -2
     * @param originalQualityIndex quality index to use, as chosen by YouTube
     */
    public static int setVideoQuality(VideoQuality[] qualities, VideoQualityMenuInterface menu, int originalQualityIndex) {
        try {
            Utils.verifyOnMainThread();
            currentMenuInterface = menu;

            final boolean availableQualitiesChanged = currentQualities == null
                    || currentQualities.size() != qualities.length;
            if (availableQualitiesChanged) {
                currentQualities = Arrays.asList(qualities);
                Logger.printDebug(() -> "VideoQualities: " + currentQualities);
            }

            VideoQuality updatedCurrentQuality = qualities[originalQualityIndex];
            if (updatedCurrentQuality.patch_getResolution() != AUTOMATIC_VIDEO_QUALITY_VALUE &&
                    (currentQuality == null
                            || !currentQuality.patch_getQualityName().equals(updatedCurrentQuality.patch_getQualityName()))) {
                currentQuality = updatedCurrentQuality;
                Logger.printDebug(() -> "Current quality changed to: " + updatedCurrentQuality);

                if (!userChangedDefaultQuality) {
                    VideoQualityDialogButton.updateButtonIcon(updatedCurrentQuality);
                }
            }

            final int preferredQuality = getDefaultQualityResolution();
            if (!userChangedDefaultQuality && preferredQuality == AUTOMATIC_VIDEO_QUALITY_VALUE) {
                return originalQualityIndex; // Nothing to do.
            }

            if (userChangedDefaultQuality) {
                userChangedDefaultQuality = false;
                VideoQuality quality = qualities[userSelectedQualityIndex];
                Logger.printDebug(() -> "User changed default quality to: " + quality);
                saveDefaultQuality(quality.patch_getResolution());
                VideoQualityDialogButton.updateButtonIcon(quality);
                return userSelectedQualityIndex;
            }

            // After changing videos the qualities can initially be for the prior video.
            // If the qualities have changed and the default is not auto then an update is needed.
            if (!qualityNeedsUpdating && !availableQualitiesChanged) {
                return originalQualityIndex;
            }
            qualityNeedsUpdating = false;

            // Find the highest quality that is equal to or less than the preferred.
            int i = 0;
            for (VideoQuality quality : qualities) {
                final int qualityResolution = quality.patch_getResolution();
                if (qualityResolution != AUTOMATIC_VIDEO_QUALITY_VALUE && qualityResolution <= preferredQuality) {
                    // If the desired quality index is equal to the original index,
                    // then the video is already set to the desired default quality.
                    if (i == originalQualityIndex) {
                        Logger.printDebug(() -> "Video is already preferred quality: " + quality);
                    } else {
                        Logger.printDebug(() -> "Changing video quality from: "
                                + qualities[originalQualityIndex] + " to: " + quality);
                    }

                    // On first load of a new video, if the video is already the desired quality
                    // then the quality flyout will show 'Auto' (ie: Auto (720p)).
                    //
                    // To prevent user confusion, set the video index even if the
                    // quality is already correct so the UI picker will not display "Auto".
                    Logger.printDebug(() -> "Changing quality to default: " + quality);
                    menu.patch_setQuality(qualities[i]);
                    return i;
                }
                i++;
            }
        } catch (Exception ex) {
            Logger.printException(() -> "setVideoQuality failure", ex);
        }
        return originalQualityIndex;
    }

    /**
     * Injection point. Fixes bad data used by YouTube.
     */
    public static int fixVideoQualityResolution(String name, int quality) {
        final int correctQuality = 480;
        if (name.equals("480p") && quality != correctQuality) {
            return correctQuality;
        }

        return quality;
    }

    /**
     * Injection point.
     * @param qualityIndex Element index of {@link #currentQualities}.
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
        if (shouldRememberVideoQuality()) {
            saveDefaultQuality(videoResolution);
        }
    }

    /**
     * Injection point.
     */
    public static void newVideoStarted(VideoInformation.PlaybackController ignoredPlayerController) {
        Utils.verifyOnMainThread();

        Logger.printDebug(() -> "newVideoStarted");
        qualityNeedsUpdating = true;
        currentQualities = null;
        currentQuality = null;
        currentMenuInterface = null;

        // Hide the quality button until playback starts and the qualities are available.
        VideoQualityDialogButton.updateButtonIcon(null);
    }
}
