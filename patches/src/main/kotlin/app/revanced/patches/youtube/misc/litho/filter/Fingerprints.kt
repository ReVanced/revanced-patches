package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.patcher.fieldAccess
import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import app.revanced.patcher.literal
import app.revanced.patcher.methodCall
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val componentCreateFingerprint = fingerprint {
    instructions(
        string("Element missing correct type extension"),
        string("Element missing type")
    )
}

internal val lithoFilterFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    custom { _, classDef ->
        classDef.endsWith("/LithoFilterPatch;")
    }
}

internal val protobufBufferReferenceFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("[B")
    instructions(
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            definingClass = "this",
            type = "Lcom/google/android/libraries/elements/adl/UpbMessage;"
        ),
        methodCall(
            definingClass = "Lcom/google/android/libraries/elements/adl/UpbMessage;",
            name = "jniDecode"
        )
    )
}

internal val protobufBufferReferenceLegacyFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("I", "Ljava/nio/ByteBuffer;")
    opcodes(
        Opcode.IPUT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.SUB_INT_2ADDR,
    )
}

internal val emptyComponentFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.CONSTRUCTOR)
    parameters()
    instructions(
        string("EmptyComponent")
    )
    custom { _, classDef ->
        classDef.methods.filter { AccessFlags.STATIC.isSet(it.accessFlags) }.size == 1
    }
}

internal val lithoThreadExecutorFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("I", "I", "I")
    custom { method, classDef ->
        classDef.superclass == "Ljava/util/concurrent/ThreadPoolExecutor;" &&
            method.containsLiteralInstruction(1L) // 1L = default thread timeout.
    }
}

internal val lithoComponentNameUpbFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    instructions(
        literal(45631264L)
    )
}

internal val lithoConverterBufferUpbFeatureFlagFingerprint = fingerprint {
    returns("L")
    instructions(
        literal(45419603L)
    )
}
