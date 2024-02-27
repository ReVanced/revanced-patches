package app.revanced.patches.music.utils.fix.clientspoof.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object UserAgentHeaderBuilderFingerprint : MethodFingerprint(
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.CONST_16
    ),
    strings = listOf("(Linux; U; Android ")
)