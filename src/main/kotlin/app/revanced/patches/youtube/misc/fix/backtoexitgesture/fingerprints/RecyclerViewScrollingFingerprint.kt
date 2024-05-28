package app.revanced.patches.youtube.misc.fix.backtoexitgesture.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val recyclerViewScrollingFingerprint = methodFingerprint {
    returns("V")
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    parameters()
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_LEZ,
        Opcode.IGET_OBJECT,
        Opcode.CONST_4,
    )
}
