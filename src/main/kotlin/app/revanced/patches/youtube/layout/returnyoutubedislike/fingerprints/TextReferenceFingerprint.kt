package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object TextReferenceFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_STATIC_RANGE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_DIRECT,
        Opcode.INVOKE_VIRTUAL
    )
)