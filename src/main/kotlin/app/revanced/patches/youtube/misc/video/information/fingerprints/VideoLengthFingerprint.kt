package app.revanced.patches.youtube.misc.video.information.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

import org.jf.dexlib2.Opcode

object VideoLengthFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.MOVE_RESULT_WIDE,
        Opcode.CMP_LONG,
        Opcode.IF_LEZ,
        Opcode.IGET_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_WIDE,
        Opcode.GOTO,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_WIDE,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL
    )
)