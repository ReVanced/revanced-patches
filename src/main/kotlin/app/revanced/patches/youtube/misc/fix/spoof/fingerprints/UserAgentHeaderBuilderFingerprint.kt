package app.revanced.patches.youtube.misc.fix.spoof.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object UserAgentHeaderBuilderFingerprint : MethodFingerprint(
    parameters = listOf("L", "L", "L"),
    opcodes = listOf(Opcode.MOVE_RESULT_OBJECT, Opcode.INVOKE_VIRTUAL),
    strings = listOf("(Linux; U; Android "),
)