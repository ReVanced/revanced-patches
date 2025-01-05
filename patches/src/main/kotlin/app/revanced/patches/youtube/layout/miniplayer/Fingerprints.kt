@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.youtube.layout.miniplayer

import app.revanced.patcher.LiteralFilter
import app.revanced.patcher.fingerprint
import app.revanced.patches.shared.misc.mapping.ResourceMappingFilter
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val miniplayerDimensionsCalculatorParentFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    instructions(
        ResourceMappingFilter("dimen", "floaty_bar_button_top_margin")
    )
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernAddViewListenerFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/view/View;")
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */

internal val miniplayerModernCloseButtonFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/widget/ImageView;")
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

internal val miniplayerModernConstructorFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("L")
    literal { 45623000L }
}

internal val miniplayerOnCloseHandlerFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    literal { MINIPLAYER_DISABLED_FEATURE_KEY  }
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernExpandButtonFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/widget/ImageView;")
    parameters()
    literal { modernMiniplayerExpand }
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernExpandCloseDrawablesFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    literal { ytOutlinePictureInPictureWhite24 }
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernForwardButtonFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/widget/ImageView;")
    parameters()
    literal { modernMiniplayerForwardButton }
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernOverlayViewFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    literal { scrimOverlay }
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernRewindButtonFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Landroid/widget/ImageView;")
    parameters()
    literal { modernMiniplayerRewindButton }
}

internal val miniplayerModernViewParentFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String;")
    parameters()
    strings("player_overlay_modern_mini_player_controls")
}

internal val miniplayerMinimumSizeFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        ResourceMappingFilter("dimen", "miniplayer_max_size"),
        LiteralFilter(192),
        LiteralFilter(128)
    )
}

internal val miniplayerOverrideFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    strings("appName")
}

internal val miniplayerOverrideNoContextFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("Z")
    opcodes(Opcode.IGET_BOOLEAN) // Anchor to insert the instruction.
}

internal val miniplayerResponseModelSizeCheckFingerprint by fingerprint {
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

internal val playerOverlaysLayoutFingerprint by fingerprint {
    custom { method, _ ->
        method.definingClass == YOUTUBE_PLAYER_OVERLAYS_LAYOUT_CLASS_NAME
    }
}
