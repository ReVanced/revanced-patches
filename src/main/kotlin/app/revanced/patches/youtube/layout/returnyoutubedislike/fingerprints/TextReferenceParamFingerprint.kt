package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object TextReferenceParamFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.MOVE_OBJECT,
        Opcode.MOVE_OBJECT_FROM16 // the first occurrence of this instruction uses the register for the text object
    )
)