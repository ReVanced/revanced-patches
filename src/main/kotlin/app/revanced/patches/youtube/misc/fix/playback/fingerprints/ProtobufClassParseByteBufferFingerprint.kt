package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object ProtobufClassParseByteBufferFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PROTECTED or AccessFlags.STATIC,
    parameters = listOf("L", "Ljava/nio/ByteBuffer;"),
    returnType = "L",
    opcodes = listOf(
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT,
    ),
    customFingerprint = { methodDef, _ -> methodDef.name == "parseFrom" },
)