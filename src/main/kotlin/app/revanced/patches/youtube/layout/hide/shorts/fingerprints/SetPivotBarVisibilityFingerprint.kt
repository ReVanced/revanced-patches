package app.revanced.patches.youtube.layout.hide.shorts.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val setPivotBarVisibilityFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("V")
    parameters("Z")
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.IF_EQZ,
    )
}
