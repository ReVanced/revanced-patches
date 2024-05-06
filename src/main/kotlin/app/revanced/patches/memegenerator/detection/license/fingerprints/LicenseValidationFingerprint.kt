package app.revanced.patches.memegenerator.detection.license.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val licenseValidationFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Z")
    parameters("Landroid/content/Context;")
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_WIDE,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_WIDE,
        Opcode.CMP_LONG,
        Opcode.IF_GEZ,
        Opcode.CONST_4,
        Opcode.RETURN,
        Opcode.CONST_4,
        Opcode.RETURN
    )
}