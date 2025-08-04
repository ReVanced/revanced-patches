package app.revanced.extension.youtube.patches.playback.quality;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.NetworkType;

import androidx.annotation.Nullable;

import com.google.android.libraries.youtube.innertube.model.media.VideoQuality;

import java.util.Arrays;

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

    /**
     * Video resolution of the automatic quality option..
     */
    public static final int AUTOMATIC_VIDEO_QUALITY_VALUE = -2;

    /**
     * All quality names are the same for all languages.
     * VideoQuality also has a resolution enum that can be used if needed.
     */
    public static final String VIDEO_QUALITY_1080P_PREMIUM_NAME = "1080p Premium";

    private static final IntegerSetting videoQualityWifi = Settings.VIDEO_QUALITY_DEFAULT_WIFI;
    private static final IntegerSetting videoQualityMobile = Settings.VIDEO_QUALITY_DEFAULT_MOBILE;
    private static final IntegerSetting shortsQualityWifi = Settings.SHORTS_QUALITY_DEFAULT_WIFI;
    private static final IntegerSetting shortsQualityMobile = Settings.SHORTS_QUALITY_DEFAULT_MOBILE;

    private static boolean qualityNeedsUpdating;

    /**
     * The available qualities of the current video.
     */
    @Nullable
    private static VideoQuality[] currentQualities;

    /**
     * The current quality of the video playing.
     * This is always the actual quality even if Automatic quality is active.
     */
    @Nullable
    private static VideoQuality currentQuality;

    /**
     * The current VideoQualityMenuInterface, set during setVideoQuality.
     */
    @Nullable
    private static VideoQualityMenuInterface currentMenuInterface;

    @Nullable
    public static VideoQuality[] getCurrentQualities() {
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
     *
     * @param qualities Video qualities available, ordered from largest to smallest, with index 0 being the 'automatic' value of -2
     * @param originalQualityIndex quality index to use, as chosen by YouTube
     */
    public static int setVideoQuality(VideoQuality[] qualities, VideoQualityMenuInterface menu, int originalQualityIndex) {
        try {
            Utils.verifyOnMainThread();
            currentMenuInterface = menu;

            final boolean availableQualitiesChanged = (currentQualities == null)
                    || !Arrays.equals(currentQualities, qualities);
            if (availableQualitiesChanged) {
                currentQualities = qualities;
                Logger.printDebug(() -> "VideoQualities: " + Arrays.toString(currentQualities));
            }

            VideoQuality updatedCurrentQuality = qualities[originalQualityIndex];
            if (updatedCurrentQuality.patch_getResolution() != AUTOMATIC_VIDEO_QUALITY_VALUE
                    && (currentQuality == null || currentQuality != updatedCurrentQuality)) {
                currentQuality = updatedCurrentQuality;
                Logger.printDebug(() -> "Current quality changed to: " + updatedCurrentQuality);

                VideoQualityDialogButton.updateButtonIcon(updatedCurrentQuality);
            }

            final int preferredQuality = getDefaultQualityResolution();
            if (preferredQuality == AUTOMATIC_VIDEO_QUALITY_VALUE) {
                return originalQualityIndex; // Nothing to do.
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
                if ((qualityResolution != AUTOMATIC_VIDEO_QUALITY_VALUE && qualityResolution <= preferredQuality)
                        // Use the lowest video quality if the default is lower than all available.
                        || i == qualities.length - 1) {
                    final boolean qualityNeedsChange = (i != originalQualityIndex);
                    Logger.printDebug(() -> qualityNeedsChange
                            ? "Changing video quality from: " + updatedCurrentQuality + " to: " + quality
                            : "Video is already the preferred quality: " + quality
                    );

                    // On first load of a new regular video, if the video is already the
                    // desired quality then the quality flyout will show 'Auto' (ie: Auto (720p)).
                    //
                    // To prevent user confusion, set the video index even if the
                    // quality is already correct so the UI picker will not display "Auto".
                    //
                    // Only change Shorts quality if the quality actually needs to change,
                    // because the "auto" option is not shown in the flyout
                    // and setting the same quality again can cause the Short to restart.
                    if (qualityNeedsChange || !ShortsPlayerState.isOpen()) {
                        menu.patch_setQuality(qualities[i]);
                        return i;
                    }

                    return originalQualityIndex;
                }
                i++;
            }
        } catch (Exception ex) {
            Logger.printException(() -> "setVideoQuality failure", ex);
        }
        return originalQualityIndex;
    }

    /**
     * Injection point.
     * @param userSelectedQualityIndex Element index of {@link #currentQualities}.
     */
    public static void userChangedShortsQuality(int userSelectedQualityIndex) {
        try {
            if (shouldRememberVideoQuality()) {
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
        currentQualities = null;
        currentQuality = null;
        currentMenuInterface = null;
        qualityNeedsUpdating = true;

        // Hide the quality button until playback starts and the qualities are available.
        VideoQualityDialogButton.updateButtonIcon(null);
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
}
