package app.revanced.patches.youtube.video.information.fingerprints


import app.revanced.patcher.fingerprint.MethodFingerprint

import com.android.tools.smali.dexlib2.Opcode

internal object VideoLengthFingerprint : MethodFingerprint(
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