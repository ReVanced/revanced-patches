@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.youtube.layout.miniplayer

import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val miniplayerDimensionsCalculatorParentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    literal { floatyBarButtonTopMargin }
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernAddViewListenerFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/view/View;")
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */

internal val miniplayerModernCloseButtonFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    literal { modernMiniplayerClose }
}

internal const val MINIPLAYER_MODERN_FEATURE_KEY = 45622882L
// In later targets this feature flag does nothing and is dead code.
internal const val MINIPLAYER_MODERN_FEATURE_LEGACY_KEY = 45630429L
internal const val MINIPLAYER_DOUBLE_TAP_FEATURE_KEY = 45628823L
internal const val MINIPLAYER_DRAG_DROP_FEATURE_KEY = 45628752L
internal const val MINIPLAYER_HORIZONTAL_DRAG_FEATURE_KEY = 45658112L
internal const val MINIPLAYER_ROUNDED_CORNERS_FEATURE_KEY = 45652224L
internal const val MINIPLAYER_INITIAL_SIZE_FEATURE_KEY = 45640023L
internal const val MINIPLAYER_DISABLED_FEATURE_KEY = 45657015L

internal val miniplayerModernConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("L")
    literal { 45623000L }
}

internal val miniplayerOnCloseHandlerFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    literal { MINIPLAYER_DISABLED_FEATURE_KEY  }
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernExpandButtonFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    literal { modernMiniplayerExpand }
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernExpandCloseDrawablesFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    literal { ytOutlinePictureInPictureWhite24 }
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernForwardButtonFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    literal { modernMiniplayerForwardButton }
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernOverlayViewFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    literal { scrimOverlay }
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernRewindButtonFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    literal { modernMiniplayerRewindButton }
}

internal val miniplayerModernViewParentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String;")
    parameters()
    strings("player_overlay_modern_mini_player_controls")
}

internal val miniplayerModernActionButtonFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    literal { modernMiniPlayerOverlayActionButton }
}

internal val miniplayerMinimumSizeFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    custom { method, _ ->
        method.containsLiteralInstruction(192) &&
            method.containsLiteralInstruction(128) &&
            method.containsLiteralInstruction(miniplayerMaxSize)
    }
}

internal val miniplayerOverrideFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    strings("appName")
}

internal val miniplayerOverrideNoContextFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("Z")
    opcodes(Opcode.IGET_BOOLEAN) // Anchor to insert the instruction.
}

internal val miniplayerResponseModelSizeCheckFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters("Ljava/lang/Object;", "Ljava/lang/Object;")
    opcodes(
        Opcode.RETURN_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.IF_NEZ,
    )
}

internal const val YOUTUBE_PLAYER_OVERLAYS_LAYOUT_CLASS_NAME =
    "Lcom/google/android/apps/youtube/app/common/player/overlay/YouTubePlayerOverlaysLayout;"

internal val playerOverlaysLayoutFingerprint = fingerprint {
    custom { method, _ ->
        method.definingClass == YOUTUBE_PLAYER_OVERLAYS_LAYOUT_CLASS_NAME
    }
}
