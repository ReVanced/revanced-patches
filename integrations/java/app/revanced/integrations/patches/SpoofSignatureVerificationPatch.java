package app.revanced.integrations.patches;

import static app.revanced.integrations.utils.ReVancedUtils.containsAny;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class SpoofSignatureVerificationPatch {
    /**
     * Protobuf parameters used for autoplay in scrim.
     * Prepend this parameter to mute video playback (for autoplay in feed)
     */
    private static final String PROTOBUF_PARAMETER_SCRIM = "SAFgAXgB";

    /**
     * Protobuf parameter of shorts and YouTube stories.
     * Known issue: captions are positioned on upper area in the player.
     */
    private static final String PROTOBUF_PARAMETER_SHORTS = "8AEB"; // "8AEByAMTuAQP"

    /**
     * Target Protobuf parameters.
     */
    private static final String[] PROTOBUF_PARAMETER_TARGETS = {
            "YAHI", // Autoplay in feed
            "SAFg"  // Autoplay in scrim
    };

    @Nullable
    private static String currentVideoId;

    /**
     * If any of the subtitles settings encountered from the current video have been non default values.
     */
    private static boolean nonDefaultSubtitlesEncountered;

    /**
     * Injection point.
     *
     * @param originalValue originalValue protobuf parameter
     */
    public static String overrideProtobufParameter(String originalValue) {
        try {
            if (!SettingsEnum.SIGNATURE_SPOOFING.getBoolean()) {
                return originalValue;
            }

            LogHelper.printDebug(() -> "Original protobuf parameter value: " + originalValue);

            // Video is Short or Story.
            var isPlayingShorts = originalValue.contains(PROTOBUF_PARAMETER_SHORTS);
            if (isPlayingShorts) return originalValue;

            boolean isPlayingFeed = containsAny(originalValue, PROTOBUF_PARAMETER_TARGETS) && PlayerType.getCurrent() == PlayerType.INLINE_MINIMAL;
            if (isPlayingFeed) {
                // Videos in feed won't autoplay with sound.
                return PROTOBUF_PARAMETER_SCRIM + PROTOBUF_PARAMETER_SHORTS;
            } else {
                // Spoof the parameter to prevent playback issues.
                return PROTOBUF_PARAMETER_SHORTS;
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "overrideProtobufParameter failure", ex);
        }

        return originalValue;
    }


    /**
     * Injection point. Runs off the main thread.
     * <p>
     * Used to check the response code of video playback requests made by YouTube.
     * Response code of interest is 403 that indicate a signature verification failure for the current request
     *
     * @param responseCode HTTP status code of the completed YouTube connection
     */
    public static void onResponse(int responseCode) {
        try {
            if (responseCode < 400 || responseCode >= 500) {
                return; // everything normal
            }
            LogHelper.printDebug(() -> "YouTube HTTP status code: " + responseCode);

            if (SettingsEnum.SIGNATURE_SPOOFING.getBoolean()) {
                return;  // already enabled
            }

            SettingsEnum.SIGNATURE_SPOOFING.saveValue(true);
            ReVancedUtils.showToastLong("Spoofing app signature to prevent playback issues");
            // it would be great if the video could be forcefully reloaded, but currently there is no code to do this

        } catch (Exception ex) {
            LogHelper.printException(() -> "onResponse failure", ex);
        }
    }

    /**
     * Last WindowsSetting constructor values. Values are checked for changes to reduce log spam.
     */
    private static int lastAp, lastAh, lastAv;
    private static boolean lastVs, lastSd;

    /**
     * Injection point.  Overrides values passed into SubtitleWindowSettings constructor.
     *
     * @param ap anchor position. A bitmask with 6 bit fields, that appears to indicate the layout position on screen
     * @param ah anchor horizontal. A percentage [0, 100], that appears to be a horizontal text anchor point
     * @param av anchor vertical. A percentage [0, 100], that appears to be a vertical text anchor point
     * @param vs appears to indicate if subtitles exist, and the value is always true.
     * @param sd function is not entirely clear
     */
    public static int[] getSubtitleWindowSettingsOverride(int ap, int ah, int av, boolean vs, boolean sd) {
        final boolean signatureSpoofing = SettingsEnum.SIGNATURE_SPOOFING.getBoolean();
        if (SettingsEnum.DEBUG.getBoolean()) {
            if (ap != lastAp || ah != lastAh || av != lastAv || vs != lastVs || sd != lastSd) {
                LogHelper.printDebug(() -> "video: " + VideoInformation.getVideoId() + " spoof: " + signatureSpoofing
                        + " ap:" + ap + " ah:" + ah + " av:" + av + " vs:" + vs + " sd:" + sd);
                lastAp = ap;
                lastAh = ah;
                lastAv = av;
                lastVs = vs;
                lastSd = sd;
            }
        }

        // Videos with custom captions that specify screen positions appear to always have correct screen positions (even with spoofing).
        // But for auto generated and most other captions, the spoof incorrectly gives various default Shorts caption settings.
        // Check for these known default shorts captions parameters, and replace with the known correct values.
        //
        // If a regular video uses a custom subtitle setting that match a default short setting,
        // then this will incorrectly replace the setting.
        // But, if the video uses multiple subtitles in different screen locations, then detect the non-default values
        // and do not replace any window settings for the video (regardless if they match a shorts default).
        if (signatureSpoofing && !nonDefaultSubtitlesEncountered && !PlayerType.getCurrent().isNoneOrHidden()) {
            for (SubtitleWindowReplacementSettings setting : SubtitleWindowReplacementSettings.values()) {
                if (setting.match(ap, ah, av, vs, sd)) {
                    return setting.replacementSetting();
                }
            }
            // Settings appear to be custom subtitles.
            nonDefaultSubtitlesEncountered = true;
            LogHelper.printDebug(() -> "Non default subtitles found. Using existing settings without replacement.");
        }

        return new int[]{ap, ah, av};
    }

    /**
     * Injection point.
     */
    public static void setCurrentVideoId(@NonNull String videoId) {
        try {
            if (videoId.equals(currentVideoId)) {
                return;
            }
            currentVideoId = videoId;
            nonDefaultSubtitlesEncountered = false;
        } catch (Exception ex) {
            LogHelper.printException(() -> "setCurrentVideoId failure", ex);
        }
    }


    /**
     * Known incorrect default Shorts subtitle parameters, and the corresponding correct (non-Shorts) values.
     */
    private enum SubtitleWindowReplacementSettings {
        DEFAULT_SHORTS_PARAMETERS_1(10, 50, 0, true, false,
                34, 50, 95),
        DEFAULT_SHORTS_PARAMETERS_2(9, 20, 0, true, false,
                34, 50, 90),
        DEFAULT_SHORTS_PARAMETERS_3(9, 20, 0, true, true,
                33, 20, 100);

        // original values
        final int ap, ah, av;
        final boolean vs, sd;

        // replacement int values
        final int[] replacement;

        SubtitleWindowReplacementSettings(int ap, int ah, int av, boolean vs, boolean sd,
                                          int replacementAp, int replacementAh, int replacementAv) {
            this.ap = ap;
            this.ah = ah;
            this.av = av;
            this.vs = vs;
            this.sd = sd;
            this.replacement = new int[]{replacementAp, replacementAh, replacementAv};
        }

        boolean match(int ap, int ah, int av, boolean vs, boolean sd) {
            return this.ap == ap && this.ah == ah && this.av == av && this.vs == vs && this.sd == sd;
        }

        int[] replacementSetting() {
            return replacement;
        }
    }
}
