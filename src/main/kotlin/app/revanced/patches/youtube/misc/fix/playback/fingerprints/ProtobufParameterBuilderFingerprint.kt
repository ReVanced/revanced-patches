package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object ProtobufParameterBuilderFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.MOVE_RESULT,
        Opcode.CONST_16,
        Opcode.MOVE_FROM16,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.MOVE_OBJECT,
        Opcode.MOVE_FROM16,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.MOVE_FROM16,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.MOVE_FROM16,
        Opcode.MOVE_FROM16,
        Opcode.MOVE_FROM16,
        Opcode.INVOKE_VIRTUAL_RANGE, // target reference
//        Opcode.MOVE_RESULT_OBJECT,
//        Opcode.IPUT_OBJECT
    ),
    strings = listOf(
        "Prefetch request are disabled.",
        "Unexpected empty videoId.",
    )
)