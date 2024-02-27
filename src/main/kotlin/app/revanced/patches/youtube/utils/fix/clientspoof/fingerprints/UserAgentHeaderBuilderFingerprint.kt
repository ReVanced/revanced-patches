package app.revanced.patches.youtube.utils.fix.clientspoof.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

object UserAgentHeaderBuilderFingerprint : MethodFingerprint(
    parameters = listOf("L", "L", "L"),
    opcodes = listOf(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL
    ),
    strings = listOf("(Linux; U; Android ")
)