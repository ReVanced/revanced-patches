package app.revanced.integrations.patches;

import androidx.annotation.Nullable;

import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.utils.LogHelper;

/**
 * Hook receiver class for 'player-type-hook' patch
 *
 * @usedBy app.revanced.patches.youtube.misc.playertype.patch.PlayerTypeHookPatch
 * @smali Lapp/revanced/integrations/patches/PlayerTypeHookPatch;
 */
@SuppressWarnings("unused")
public class PlayerTypeHookPatch {
    /**
     * Hook into YouTubePlayerOverlaysLayout.updatePlayerLayout() method
     *
     * @param type the new player type
     * @smali YouTubePlayerOverlaysLayout_updatePlayerTypeHookEX(Ljava/lang/Object;)V
     */
    public static void YouTubePlayerOverlaysLayout_updatePlayerTypeHookEX(@Nullable Object type) {
        if (type == null) return;

        // update current player type
        final PlayerType newType = PlayerType.safeParseFromString(type.toString());
        if (newType == null) {
            LogHelper.printException(() -> "Unknown PlayerType encountered: " + type);
        } else {
            PlayerType.setCurrent(newType);
            LogHelper.printDebug(() -> "PlayerType was updated to: " + newType);
        }
    }
}
