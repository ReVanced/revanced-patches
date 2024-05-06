package app.revanced.patches.instagram.patches.ads.timeline.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val showAdFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
    returns("Z")
    parameters("L", "L", "Z", "Z")
    opcodes(
        Opcode.SGET_OBJECT,
        Opcode.IF_NE,
        Opcode.IF_NEZ,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.IF_NEZ,
        Opcode.RETURN,
    )
}
