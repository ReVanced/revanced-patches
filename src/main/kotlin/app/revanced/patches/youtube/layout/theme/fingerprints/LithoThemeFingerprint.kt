package app.revanced.patches.youtube.layout.theme.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val lithoThemeFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/graphics/Rect;")
    opcodes(
        Opcode.APUT,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.IGET_OBJECT,
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IPUT_OBJECT,
        Opcode.IGET,
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_NEZ,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID,
    )
    custom { methodDef, _ ->
        methodDef.name == "onBoundsChange"
    }
}
