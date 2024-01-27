package app.revanced.patches.youtube.video.videoid.fingerprint

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object VideoIdFingerprintBackgroundPlay : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.DECLARED_SYNCHRONIZED or AccessFlags.FINAL or AccessFlags.PUBLIC,
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
