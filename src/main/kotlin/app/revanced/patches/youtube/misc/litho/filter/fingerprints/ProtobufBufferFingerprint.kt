package app.revanced.patches.youtube.misc.litho.filter.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object ProtobufBufferFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.IGET_OBJECT, // References the field required below.
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        Opcode.IF_NEZ,
        Opcode.CONST_4,
        Opcode.GOTO,
        Opcode.CHECK_CAST, // Casts the referenced field to a specific type that stores the protobuf buffer.
        Opcode.INVOKE_VIRTUAL
    )
)