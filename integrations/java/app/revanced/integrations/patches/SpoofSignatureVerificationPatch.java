package app.revanced.integrations.patches;

import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.utils.LogHelper;

public class SpoofSignatureVerificationPatch {

    /**
     * Protobuf parameters used by the player.
     * Known issue: YouTube client recognizes generic player as shorts video.
     */
    private static final String GENERAL_PROTOBUF_PARAMETER = "CgIQBg";

    /**
     * Protobuf parameter of short and YouTube story.
     */
    private static final String SHORTS_PROTOBUF_PARAMETER = "8AEB"; // "8AEByAMTuAQP"

    /**
     * Target Protobuf parameters.
     * Used by the generic player.
     */
    private static final String TARGET_PROTOBUF_PARAMETER = "YADI";

    public static String getVerificationSpoofOverride(String original) {
        PlayerType player = PlayerType.getCurrent();
        LogHelper.printDebug(() -> "Original protobuf parameter value: " + original + " PlayerType: " + player);
        if (original.startsWith(TARGET_PROTOBUF_PARAMETER)  || original.length() == 0) {
            if (player == PlayerType.INLINE_MINIMAL) {
                return GENERAL_PROTOBUF_PARAMETER; // home feed autoplay
            }
            if (player.isNoneOrHidden()) {
                return SHORTS_PROTOBUF_PARAMETER; // short or story
            }
            return SHORTS_PROTOBUF_PARAMETER; // regular video player
        }

        return original;
    }
}
