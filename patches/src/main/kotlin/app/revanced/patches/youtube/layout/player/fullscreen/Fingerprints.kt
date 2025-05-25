package app.revanced.patches.youtube.layout.player.fullscreen

import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * 19.46+
 */
internal val openVideosFullscreenPortraitFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "Lj\$/util/Optional;")
    instructions(
        opcode(Opcode.MOVE_RESULT), // Conditional check to modify.
        // Open videos fullscreen portrait feature flag.
        literal(45666112L, maxAfter = 5), // Cannot be more than 5.
        opcode(Opcode.MOVE_RESULT, maxAfter = 10),
    )
}

/**
 * Pre 19.46.
 */
internal val openVideosFullscreenPortraitLegacyFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "Lj\$/util/Optional;")
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
        Opcode.MOVE_RESULT  // Conditional check to modify.
    )
}

internal val openVideosFullscreenHookPatchExtensionFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.STATIC)
    returns("Z")
    parameters()
    custom { methodDef, classDef ->
        methodDef.name == "isFullScreenPatchIncluded" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
