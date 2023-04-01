package app.revanced.integrations.patches;

import android.widget.Toast;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

import static app.revanced.integrations.utils.ReVancedUtils.containsAny;

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
            } else{
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
            ReVancedUtils.runOnMainThread(() -> {
                Toast.makeText(
                        ReVancedUtils.getContext(),
                        "Spoofing app signature to prevent playback issues", Toast.LENGTH_LONG
                ).show();
                // it would be great if the video could be forcefully reloaded, but currently there is no code to do this
            });

        } catch (Exception ex) {
            LogHelper.printException(() -> "onResponse failure", ex);
        }
    }

    /**
     * Last WindowsSetting constructor values. Values are checked for changes to reduce log spam.
     */
    private static int lastAnchorPosition, lastAnchorHorizontal, lastAnchorVertical;
    private static boolean lastVs, lastSd;

    /**
     * Injection point.  Overrides values passed into SubtitleWindowSettings constructor.
     *
     * @param anchorPosition       bitmask with 6 bit fields, that appears to be indicate the layout position on screen
     * @param anchorHorizontal     percentage [0, 100], that appears to be a horizontal text anchor point
     * @param anchorVertical       percentage [0, 100], that appears to be a vertical text anchor point
     * @param vs                   appears to indicate is subtitles exist, and value is always true.
     * @param sd                   appears to indicate if video has non standard aspect ratio (4:3, or a rotated orientation)
     *                             Always true for Shorts playback.
     */
    public static int[] getSubtitleWindowSettingsOverride(int anchorPosition, int anchorHorizontal, int anchorVertical,
                                                         boolean vs, boolean sd) {
        int[] override = {anchorPosition, anchorHorizontal, anchorVertical};

        // Videos with custom captions that specify screen positions appear to always have correct screen positions (even with spoofing).
        // But for auto generated and most other captions, the spoof incorrectly gives Shorts caption settings for all videos.
        // Override the parameters if the video is not a Short but it has Short caption settings.
        if (SettingsEnum.SIGNATURE_SPOOFING.getBoolean()
                && !PlayerType.getCurrent().isNoneOrHidden() // video is not a Short or Story
                && anchorPosition == 9 // but it has shorts specific subtitle parameters
                && anchorHorizontal == 20
                && anchorVertical == 0) {
            if (sd) {
                // values observed during playback
                override[0] = 33;
                override[1] = 20;
                override[2] = 100;
            } else {
                // Default values used for regular (non Shorts) playback of videos with a standard aspect ratio
                // Values are found in SubtitleWindowSettings static field
                override[0] = 34;
                override[1] = 50;
                override[2] = 95;
            }
        }

        if (!SettingsEnum.DEBUG.getBoolean()) {
            return override;
        }
        if (anchorPosition != lastAnchorPosition
                || anchorHorizontal != lastAnchorHorizontal || anchorVertical != lastAnchorVertical
                || vs != lastVs || sd != lastSd) {
            LogHelper.printDebug(() -> "SubtitleWindowSettings anchorPosition:" + anchorPosition
                    + " anchorHorizontal:" + anchorHorizontal + " anchorVertical:" + anchorVertical
                    + " vs:" + vs + " sd:" + sd);
            lastAnchorPosition = anchorPosition;
            lastAnchorHorizontal = anchorHorizontal;
            lastAnchorVertical = anchorVertical;
            lastVs = vs;
            lastSd = sd;
        }

        return override;
    }
}
