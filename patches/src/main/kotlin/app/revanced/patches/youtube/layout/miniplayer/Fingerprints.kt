@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.youtube.layout.miniplayer

import app.revanced.patcher.InstructionLocation.MatchAfterWithin
import app.revanced.patcher.checkCast
import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.opcode
import app.revanced.patcher.string
import app.revanced.patches.shared.misc.mapping.ResourceType
import app.revanced.patches.shared.misc.mapping.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal const val MINIPLAYER_MODERN_FEATURE_KEY = 45622882L
// In later targets this feature flag does nothing and is dead code.
internal const val MINIPLAYER_MODERN_FEATURE_LEGACY_KEY = 45630429L
internal const val MINIPLAYER_DOUBLE_TAP_FEATURE_KEY = 45628823L
internal const val MINIPLAYER_DRAG_DROP_FEATURE_KEY = 45628752L
internal const val MINIPLAYER_HORIZONTAL_DRAG_FEATURE_KEY = 45658112L
internal const val MINIPLAYER_ROUNDED_CORNERS_FEATURE_KEY = 45652224L
internal const val MINIPLAYER_INITIAL_SIZE_FEATURE_KEY = 45640023L
internal const val MINIPLAYER_DISABLED_FEATURE_KEY = 45657015L
internal const val MINIPLAYER_ANIMATED_EXPAND_FEATURE_KEY = 45644360L

internal val miniplayerModernConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        literal(45623000L) // Magic number found in the constructor.
    )
}

internal val miniplayerDimensionsCalculatorParentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    instructions(
        resourceLiteral(ResourceType.DIMEN, "floaty_bar_button_top_margin")
    )
}

internal val miniplayerModernViewParentFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/String;")
    parameters()
    instructions(
        string("player_overlay_modern_mini_player_controls")
    )
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
    instructions(
        resourceLiteral(ResourceType.ID, "modern_miniplayer_close"),
        checkCast("Landroid/widget/ImageView;")
    )
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernExpandButtonFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    instructions(
        resourceLiteral(ResourceType.ID, "modern_miniplayer_expand"),
        checkCast("Landroid/widget/ImageView;")
    )
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernExpandCloseDrawablesFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L")
    instructions(
        literal(ytOutlinePictureInPictureWhite24)
    )
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernForwardButtonFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    instructions(
        resourceLiteral(ResourceType.ID, "modern_miniplayer_forward_button"),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterWithin(5))
    )
}

internal val miniplayerModernOverlayViewFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters()
    instructions(
        resourceLiteral(ResourceType.ID, "scrim_overlay"),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterWithin(5))
    )
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernRewindButtonFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    instructions(
        resourceLiteral(ResourceType.ID, "modern_miniplayer_rewind_button"),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterWithin(5))
    )
}

/**
 * Matches using the class found in [miniplayerModernViewParentFingerprint].
 */
internal val miniplayerModernActionButtonFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    parameters()
    instructions(
        resourceLiteral(ResourceType.ID, "modern_miniplayer_overlay_action_button"),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterWithin(5))
    )
}

internal val miniplayerMinimumSizeFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        resourceLiteral(ResourceType.DIMEN, "miniplayer_max_size"),
        literal(192), // Default miniplayer width constant.
        literal(128)  // Default miniplayer height constant.
    )
}

internal val miniplayerOverrideFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("L")
    instructions(
        string("appName"),
        methodCall(
            parameters = listOf("Landroid/content/Context;"),
            returnType = "Z",
            location = MatchAfterWithin(10)
        )
    )
}

internal val miniplayerOverrideNoContextFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("Z")
    instructions(
        opcode(Opcode.IGET_BOOLEAN) // Anchor to insert the instruction.
    )
}

/**
 * 20.36 and lower. Codes appears to be removed in 20.37+
 */
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

internal val miniplayerOnCloseHandlerFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    instructions(
        literal(MINIPLAYER_DISABLED_FEATURE_KEY)
    )
}

internal const val YOUTUBE_PLAYER_OVERLAYS_LAYOUT_CLASS_NAME =
    "Lcom/google/android/apps/youtube/app/common/player/overlay/YouTubePlayerOverlaysLayout;"

internal val playerOverlaysLayoutFingerprint = fingerprint {
    custom { method, _ ->
        method.definingClass == YOUTUBE_PLAYER_OVERLAYS_LAYOUT_CLASS_NAME
    }
}

internal val miniplayerSetIconsFingerprint = fingerprint {
    returns("V")
    parameters("I", "Ljava/lang/Runnable;")
    instructions(
        resourceLiteral(ResourceType.DRAWABLE, "yt_fill_pause_white_36"),
        resourceLiteral(ResourceType.DRAWABLE, "yt_fill_pause_black_36")
    )
}
