package app.revanced.integrations.youtube.patches.playback.quality;

import androidx.annotation.Nullable;

import app.revanced.integrations.shared.settings.IntegerSetting;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static app.revanced.integrations.shared.Utils.NetworkType;

@SuppressWarnings("unused")
public class RememberVideoQualityPatch {
    private static final int AUTOMATIC_VIDEO_QUALITY_VALUE = -2;
    private static final IntegerSetting wifiQualitySetting = Settings.VIDEO_QUALITY_DEFAULT_WIFI;
    private static final IntegerSetting mobileQualitySetting = Settings.VIDEO_QUALITY_DEFAULT_MOBILE;

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

    private static void changeDefaultQuality(int defaultQuality) {
        String networkTypeMessage;
        if (Utils.getNetworkType() == NetworkType.MOBILE) {
            mobileQualitySetting.save(defaultQuality);
            networkTypeMessage = "mobile";
        } else {
            wifiQualitySetting.save(defaultQuality);
            networkTypeMessage = "Wi-Fi";
        }
        Utils.showToastShort("Changed default " + networkTypeMessage
                + " quality to: " + defaultQuality +"p");
    }

    /**
     * Injection point.
     *
     * @param qualities Video qualities available, ordered from largest to smallest, with index 0 being the 'automatic' value of -2
     * @param originalQualityIndex quality index to use, as chosen by YouTube
     */
    public static int setVideoQuality(Object[] qualities, final int originalQualityIndex, Object qInterface, String qIndexMethod) {
        try {
            if (!(qualityNeedsUpdating || userChangedDefaultQuality) || qInterface == null) {
                return originalQualityIndex;
            }
            qualityNeedsUpdating = false;

            final int preferredQuality;
            if (Utils.getNetworkType() == NetworkType.MOBILE) {
                preferredQuality = mobileQualitySetting.get();
            } else {
                preferredQuality = wifiQualitySetting.get();
            }
            if (!userChangedDefaultQuality && preferredQuality == AUTOMATIC_VIDEO_QUALITY_VALUE) {
                return originalQualityIndex; // nothing to do
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
                Logger.printDebug(() -> "videoQualities: " + videoQualities);
            }

            if (userChangedDefaultQuality) {
                userChangedDefaultQuality = false;
                final int quality = videoQualities.get(userSelectedQualityIndex);
                Logger.printDebug(() -> "User changed default quality to: " + quality);
                changeDefaultQuality(quality);
                return userSelectedQualityIndex;
            }

            // find the highest quality that is equal to or less than the preferred
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
            //
            // The method could return here, but the UI video quality flyout will still
            // show 'Auto' (ie: Auto (480p))
            // It appears that "Auto" picks the resolution on video load,
            // and it does not appear to change the resolution during playback.
            //
            // To prevent confusion, set the video index anyways (even if it matches the existing index)
            // As that will force the UI picker to not display "Auto" which may confuse the user.
            if (qualityIndexToUse == originalQualityIndex) {
                Logger.printDebug(() -> "Video is already preferred quality: " + preferredQuality);
            } else {
                final int qualityToUseLog = qualityToUse;
                Logger.printDebug(() -> "Quality changed from: "
                        + videoQualities.get(originalQualityIndex) + " to: " + qualityToUseLog);
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
        if (!Settings.REMEMBER_VIDEO_QUALITY_LAST_SELECTED.get()) return;

        userSelectedQualityIndex = selectedQualityIndex;
        userChangedDefaultQuality = true;
    }

    /**
     * Injection point.  New quality menu.
     */
    public static void userChangedQualityInNewFlyout(int selectedQuality) {
        if (!Settings.REMEMBER_VIDEO_QUALITY_LAST_SELECTED.get()) return;

        changeDefaultQuality(selectedQuality); // Quality is human readable resolution (ie: 1080).
    }

    /**
     * Injection point.
     */
    public static void newVideoStarted(Object ignoredPlayerController) {
        Logger.printDebug(() -> "newVideoStarted");
        qualityNeedsUpdating = true;
        videoQualities = null;
    }
}
