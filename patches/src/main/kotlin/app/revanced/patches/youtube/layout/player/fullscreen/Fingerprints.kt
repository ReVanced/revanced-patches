package app.revanced.patches.youtube.layout.player.fullscreen

import app.revanced.patcher.accessFlags
import app.revanced.patcher.after
import app.revanced.patcher.afterAtMost
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.literal
import app.revanced.patcher.opcode
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * 19.46+
 */
internal val BytecodePatchContext.openVideosFullscreenPortraitMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    parameterTypes("L", "Lj\$/util/Optional;")
    instructions(
        Opcode.MOVE_RESULT(), // Conditional check to modify.
        // Open videos fullscreen portrait feature flag.
        literal(45666112L, afterAtMost(5)), // Cannot be more than 5.
        afterAtMost(10, Opcode.MOVE_RESULT()),
    )
}

/**
 * Pre 19.46.
 */
internal val BytecodePatchContext.openVideosFullscreenPortraitLegacyMethod by gettingFirstMethodDeclaratively {
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
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returnType("Z")
    parameterTypes()
    custom { methodDef, classDef ->
        methodDef.name == "isFullScreenPatchIncluded" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
