@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.youtube.layout.miniplayer

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.shared.misc.mapping.ResourceType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef

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

internal val BytecodePatchContext.miniplayerModernConstructorMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        45623000L(), // Magic number found in the constructor.
    )
}

internal val BytecodePatchContext.miniplayerDimensionsCalculatorParentMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
    instructions(
        ResourceType.DIMEN("floaty_bar_button_top_margin"),
    )
}

internal val BytecodePatchContext.miniplayerModernViewParentMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/String;")
    parameterTypes()
    instructions(
        "player_overlay_modern_mini_player_controls"(),
    )
}

/**
 * Matches using the class found in [miniplayerModernViewParentMethod].
 */
context(_: BytecodePatchContext)
internal fun ClassDef.getMiniplayerModernAddViewListenerMethod() = firstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/view/View;")
}

/**
 * Matches using the class found in [miniplayerModernViewParentMethod].
 */
internal val miniplayerModernCloseButtonMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes()
    instructions(
        ResourceType.ID("modern_miniplayer_close"),
        allOf(Opcode.CHECK_CAST(), "Landroid/widget/ImageView;"()),
    )
}

/**
 * Matches using the class found in [miniplayerModernViewParentMethod].
 */
internal val miniplayerModernExpandButtonMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes()
    instructions(
        ResourceType.ID("modern_miniplayer_expand"),
        allOf(Opcode.CHECK_CAST(), "Landroid/widget/ImageView;"()),
    )
}

/**
 * Matches using the class found in [miniplayerModernViewParentMethod].
 */
context(_: BytecodePatchContext)
internal fun ClassDef.getMiniplayerModernExpandCloseDrawablesMethod() = firstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L")
    instructions(
        ytOutlinePictureInPictureWhite24(),
    )
}

/**
 * Matches using the class found in [miniplayerModernViewParentMethod].
 */
internal val miniplayerModernForwardButtonMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes()
    instructions(
        ResourceType.ID("modern_miniplayer_forward_button"),
        afterAtMost(5, Opcode.MOVE_RESULT_OBJECT()),
    )
}

internal val miniplayerModernOverlayViewMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes()
    instructions(
        ResourceType.ID("scrim_overlay"),
        afterAtMost(5, Opcode.MOVE_RESULT_OBJECT()),
    )
}

/**
 * Matches using the class found in [miniplayerModernViewParentMethod].
 */
internal val miniplayerModernRewindButtonMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes()
    instructions(
        ResourceType.ID("modern_miniplayer_rewind_button"),
        afterAtMost(5, Opcode.MOVE_RESULT_OBJECT()),
    )
}

/**
 * Matches using the class found in [miniplayerModernViewParentMethod].
 */
internal val miniplayerModernActionButtonMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes()
    instructions(
        ResourceType.ID("modern_miniplayer_overlay_action_button"),
        afterAtMost(5, Opcode.MOVE_RESULT_OBJECT()),
    )
}

internal val miniplayerMinimumSizeMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    instructions(
        ResourceType.DIMEN("miniplayer_max_size"),
        192L(), // Default miniplayer width constant.
        128L(), // Default miniplayer height constant.
    )
}

internal val miniplayerOverrideMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    instructions(
        "appName"(),
        afterAtMost(
            10,
            method { parameterTypes.count() == 1 && parameterTypes.first() == "Landroid/content/Context;" && returnType == "Z" },
        ),
    )
}

context(_: BytecodePatchContext)
internal fun ClassDef.getMiniplayerOverrideNoContextMethod() = firstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("Z")
    opcodes(
        Opcode.IGET_BOOLEAN, // Anchor to insert the instruction.
    )
}

/**
 * 20.36 and lower. Codes appears to be removed in 20.37+
 */
internal val miniplayerResponseModelSizeCheckMethodMatch = firstMethodComposite {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("L")
    parameterTypes("Ljava/lang/Object;", "Ljava/lang/Object;")
    opcodes(
        Opcode.RETURN_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.IF_NEZ,
    )
}

internal val BytecodePatchContext.miniplayerOnCloseHandlerMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    instructions(
        MINIPLAYER_DISABLED_FEATURE_KEY(),
    )
}

internal const val YOUTUBE_PLAYER_OVERLAYS_LAYOUT_CLASS_NAME =
    "Lcom/google/android/apps/youtube/app/common/player/overlay/YouTubePlayerOverlaysLayout;"

internal val BytecodePatchContext.playerOverlaysLayoutMethod by gettingFirstMethodDeclaratively {
    definingClass(YOUTUBE_PLAYER_OVERLAYS_LAYOUT_CLASS_NAME)
}

internal val BytecodePatchContext.miniplayerSetIconsMethod by gettingFirstMutableMethodDeclaratively {
    returnType("V")
    parameterTypes("I", "Ljava/lang/Runnable;")
    instructions(
        ResourceType.DRAWABLE("yt_fill_pause_white_36"),
        ResourceType.DRAWABLE("yt_fill_pause_black_36"),
    )
}
