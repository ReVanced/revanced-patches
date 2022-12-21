package app.revanced.patches.tasker.license.unlock.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object CheckLicenseFingerprint : MethodFingerprint(
    strings = listOf("just(IsLicensedResult(true))"),
    opcodes = listOf(
        Opcode.GOTO,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL
    )
)