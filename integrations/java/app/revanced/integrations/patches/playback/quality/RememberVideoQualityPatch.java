package app.revanced.integrations.patches.playback.quality;

import static app.revanced.integrations.utils.ReVancedUtils.NetworkType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class RememberVideoQualityPatch {
    private static final int AUTOMATIC_VIDEO_QUALITY_VALUE = -2;
    private static final SettingsEnum wifiQualitySetting = SettingsEnum.VIDEO_QUALITY_DEFAULT_WIFI;
    private static final SettingsEnum mobileQualitySetting = SettingsEnum.VIDEO_QUALITY_DEFAULT_MOBILE;

    private static boolean qualityNeedsUpdating;
    @Nullable
    private static String currentVideoId;

    /**
     * If the user selected a new quality from the flyout menu,
     * and {@link SettingsEnum#VIDEO_QUALITY_REMEMBER_LAST_SELECTED} is enabled.
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
        NetworkType networkType = ReVancedUtils.getNetworkType();
        if (networkType == NetworkType.NONE) {
            ReVancedUtils.showToastShort("No internet connection");
            return;
        }
        String networkTypeMessage;
        if (networkType == NetworkType.MOBILE) {
            mobileQualitySetting.saveValue(defaultQuality);
            networkTypeMessage = "mobile";
        } else {
            wifiQualitySetting.saveValue(defaultQuality);
            networkTypeMessage = "Wi-Fi";
        }
        ReVancedUtils.showToastShort("Changed default " + networkTypeMessage
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
            if (ReVancedUtils.getNetworkType() == NetworkType.MOBILE) {
                preferredQuality = mobileQualitySetting.getInt();
            } else {
                preferredQuality = wifiQualitySetting.getInt();
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
                LogHelper.printDebug(() -> "VideoId: " + currentVideoId + " videoQualities: " + videoQualities);
            }

            if (userChangedDefaultQuality) {
                userChangedDefaultQuality = false;
                final int quality = videoQualities.get(userSelectedQualityIndex);
                LogHelper.printDebug(() -> "User changed default quality to: " + quality);
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
            if (qualityIndexToUse == originalQualityIndex) {
                LogHelper.printDebug(() -> "Video is already preferred quality: " + preferredQuality);
                return originalQualityIndex;
            }

            final int qualityToUseLog = qualityToUse;
            LogHelper.printDebug(() -> "Quality changed from: "
                    + videoQualities.get(originalQualityIndex) + " to: " + qualityToUseLog);

            Method m = qInterface.getClass().getMethod(qIndexMethod, Integer.TYPE);
            m.invoke(qInterface, qualityToUse);
            return qualityIndexToUse;
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to set quality", ex);
            return originalQualityIndex;
        }
    }

    /**
     * Injection point.
     */
    public static void userChangedQuality(int selectedQuality) {
        if (!SettingsEnum.VIDEO_QUALITY_REMEMBER_LAST_SELECTED.getBoolean()) return;

        userSelectedQualityIndex = selectedQuality;
        userChangedDefaultQuality = true;
    }

    /**
     * Injection point.
     */
    public static void newVideoStarted(@NonNull String videoId) {
        // The same videoId can be passed in multiple times for a single video playback.
        // Such as closing and opening the app, and sometimes when turning off/on the device screen.
        //
        // Known limitation, if:
        // 1. a default video quality exists, and remember quality is turned off
        // 2. user opens a video
        // 3. user changes the video quality
        // 4. user turns off then on the device screen (or does anything else that triggers the video id hook)
        // result: the video quality of the current video will revert back to the saved default
        //
        // qualityNeedsUpdating could be set only when the videoId changes
        // but then if the user closes and re-opens the same video the default video quality will not be applied.
        LogHelper.printDebug(() -> "newVideoStarted: " + videoId);
        qualityNeedsUpdating = true;

        if (!videoId.equals(currentVideoId)) {
            currentVideoId = videoId;
            videoQualities = null;
        }
    }
}
