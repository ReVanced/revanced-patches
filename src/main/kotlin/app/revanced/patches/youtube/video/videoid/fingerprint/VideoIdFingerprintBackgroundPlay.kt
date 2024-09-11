package app.revanced.patches.youtube.video.videoid.fingerprint

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object VideoIdFingerprintBackgroundPlay : MethodFingerprint(
    returnType = "V",
    // accessFlags are "public final synchronized", or "(package protected) final synchronized"
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.IF_EQZ,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
    ),
)
