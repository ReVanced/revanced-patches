package app.revanced.patches.youtube.layout.player.fullscreen

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * 19.46+
 */
internal val BytecodePatchContext.openVideosFullscreenPortraitMethodMatch by composingFirstMethod {
    returnType("V")
    parameterTypes("L", "Lj\$/util/Optional;")
    instructions(
        Opcode.MOVE_RESULT(), // Conditional check to modify.
        // Open videos fullscreen portrait feature flag.
        afterAtMost(5, 45666112L()), // Cannot be more than 5.
        afterAtMost(10, Opcode.MOVE_RESULT()),
    )
}

/**
 * Pre 19.46.
 */
internal val BytecodePatchContext.openVideosFullscreenPortraitLegacyMethodMatch by composingFirstMethod {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L", "Lj\$/util/Optional;")
    opcodes(
        Opcode.GOTO,
        Opcode.SGET_OBJECT,
        Opcode.GOTO,
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQ,
        Opcode.IF_EQ,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT, // Conditional check to modify.
    )
}

internal val BytecodePatchContext.openVideosFullscreenHookPatchExtensionMethod by gettingFirstMethodDeclaratively {
    name("isFullScreenPatchIncluded")
    definingClass(EXTENSION_CLASS_DESCRIPTOR)
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Z")
    parameterTypes()
}
