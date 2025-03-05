package app.revanced.extension.youtube.patches.playback.quality;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.shared.Utils.NetworkType;

import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
    private static final int AUTOMATIC_VIDEO_QUALITY_VALUE = -2;
    private static final IntegerSetting videoWifiQuality = Settings.VIDEO_QUALITY_DEFAULT_WIFI;
    private static final IntegerSetting videoMobileQuality = Settings.VIDEO_QUALITY_DEFAULT_MOBILE;
    private static final IntegerSetting shortsWifiQuality = Settings.SHORTS_QUALITY_DEFAULT_WIFI;
    private static final IntegerSetting shortsMobileQuality = Settings.SHORTS_QUALITY_DEFAULT_MOBILE;

    private static boolean qualityNeedsUpdating;

    /**
     * If the user selected a new quality from the flyout menu,
     * and {@link Settings#REMEMBER_VIDEO_QUALITY_LAST_SELECTED} is enabled.
     */
    private static boolean userChangedDefaultQuality;

    /**
     * Index of the video quality chosen by the user from the flyout menu.
     */
    private static int userSelectedQualityIndex;

    /**
     * The available qualities of the current video in human readable form: [1080, 720, 480]
     */
    @Nullable
    private static List<Integer> videoQualities;

    private static boolean shouldUseShortsPreference() {
        return ShortsPlayerState.isOpen();
    }

    private static boolean shouldRememberVideoQuality() {
        BooleanSetting preference = shouldUseShortsPreference() ?
                Settings.REMEMBER_SHORTS_QUALITY_LAST_SELECTED
                : Settings.REMEMBER_VIDEO_QUALITY_LAST_SELECTED;
        return preference.get();
    }

    private static void changeDefaultQuality(int defaultQuality) {
        String networkTypeMessage;
        boolean useShortsPreference = shouldUseShortsPreference();
        if (Utils.getNetworkType() == NetworkType.MOBILE) {
            if (useShortsPreference) shortsMobileQuality.save(defaultQuality);
            else videoMobileQuality.save(defaultQuality);
            networkTypeMessage = str("revanced_remember_video_quality_mobile");
        } else {
            if (useShortsPreference) shortsWifiQuality.save(defaultQuality);
            else videoWifiQuality.save(defaultQuality);
            networkTypeMessage = str("revanced_remember_video_quality_wifi");
        }
        Utils.showToastShort(str(
                useShortsPreference ? "revanced_remember_video_quality_toast_shorts" : "revanced_remember_video_quality_toast",
                networkTypeMessage, (defaultQuality + "p")
        ));
    }

    /**
     * Injection point.
     *
     * @param qualities Video qualities available, ordered from largest to smallest, with index 0 being the 'automatic' value of -2
     * @param originalQualityIndex quality index to use, as chosen by YouTube
     */
    public static int setVideoQuality(Object[] qualities, final int originalQualityIndex, Object qInterface, String qIndexMethod) {
        try {
            boolean useShortsPreference = shouldUseShortsPreference();
            final int preferredQuality = Utils.getNetworkType() == NetworkType.MOBILE
                    ? (useShortsPreference ? shortsMobileQuality : videoMobileQuality).get()
                    : (useShortsPreference ? shortsWifiQuality : videoWifiQuality).get();

            if (!userChangedDefaultQuality && preferredQuality == AUTOMATIC_VIDEO_QUALITY_VALUE) {
                return originalQualityIndex; // Nothing to do.
            }

            if (videoQualities == null || videoQualities.size() != qualities.length) {
                videoQualities = new ArrayList<>(qualities.length);
                for (Object streamQuality : qualities) {
                    for (Field field : streamQuality.getClass().getFields()) {
                        if (field.getType().isAssignableFrom(Integer.TYPE)
                                && field.getName().length() <= 2) {
                            videoQualities.add(field.getInt(streamQuality));
                        }
                    }
                }
                
                // After changing videos the qualities can initially be for the prior video.
                // So if the qualities have changed an update is needed.
                qualityNeedsUpdating = true;
                Logger.printDebug(() -> "VideoQualities: " + videoQualities);
            }

            if (userChangedDefaultQuality) {
                userChangedDefaultQuality = false;
                final int quality = videoQualities.get(userSelectedQualityIndex);
                Logger.printDebug(() -> "User changed default quality to: " + quality);
                changeDefaultQuality(quality);
                return userSelectedQualityIndex;
            }

            if (!qualityNeedsUpdating) {
                return originalQualityIndex;
            }
            qualityNeedsUpdating = false;

            // Find the highest quality that is equal to or less than the preferred.
            int qualityToUse = videoQualities.get(0); // first element is automatic mode
            int qualityIndexToUse = 0;
            int i = 0;
            for (Integer quality : videoQualities) {
                if (quality <= preferredQuality && qualityToUse < quality)  {
                    qualityToUse = quality;
                    qualityIndexToUse = i;
                }
                i++;
            }

            // If the desired quality index is equal to the original index,
            // then the video is already set to the desired default quality.
            final int qualityToUseFinal = qualityToUse;
            if (qualityIndexToUse == originalQualityIndex) {
                // On first load of a new video, if the UI video quality flyout menu
                // is not updated then it will still show 'Auto' (ie: Auto (480p)),
                // even though it's already set to the desired resolution.
                //
                // To prevent confusion, set the video index anyways (even if it matches the existing index)
                // as that will force the UI picker to not display "Auto".
                Logger.printDebug(() -> "Video is already preferred quality: " + qualityToUseFinal);
            } else {
                Logger.printDebug(() -> "Changing video quality from: "
                        + videoQualities.get(originalQualityIndex) + " to: " + qualityToUseFinal);
            }

            Method m = qInterface.getClass().getMethod(qIndexMethod, Integer.TYPE);
            m.invoke(qInterface, qualityToUse);
            return qualityIndexToUse;
        } catch (Exception ex) {
            Logger.printException(() -> "Failed to set quality", ex);
            return originalQualityIndex;
        }
    }

    /**
     * Injection point.  Old quality menu.
     */
    public static void userChangedQuality(int selectedQualityIndex) {
        if (!shouldRememberVideoQuality()) return;

        userSelectedQualityIndex = selectedQualityIndex;
        userChangedDefaultQuality = true;
    }

    /**
     * Injection point.  New quality menu.
     */
    public static void userChangedQualityInNewFlyout(int selectedQuality) {
        if (!shouldRememberVideoQuality()) return;

        changeDefaultQuality(selectedQuality); // Quality is human readable resolution (ie: 1080).
    }

    /**
     * Injection point.
     */
    public static void newVideoStarted(VideoInformation.PlaybackController ignoredPlayerController) {
        Logger.printDebug(() -> "newVideoStarted");
        qualityNeedsUpdating = true;
        videoQualities = null;
    }
}
