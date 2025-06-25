package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.patcher.fingerprint
import app.revanced.util.containsLiteralInstruction
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val componentContextParserFingerprint = fingerprint {
    strings(
        "TreeNode result must be set.",
        // String is a partial match and changed slightly in 20.03+
        "it was removed due to duplicate converter bindings."
    )
}

/**
 * Resolves to the class found in [componentContextParserFingerprint].
 * When patching 19.16 this fingerprint matches the same method as [componentContextParserFingerprint].
 */
internal val componentContextSubParserFingerprint = fingerprint {
    strings(
        "Number of bits must be positive"
    )
}

internal val lithoFilterFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    custom { _, classDef ->
        classDef.endsWith("/LithoFilterPatch;")
    }
}

internal val protobufBufferReferenceFingerprint = fingerprint {
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
    strings("EmptyComponent")
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
    literal { 45631264L }
}

internal val lithoConverterBufferUpbFeatureFlagFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("L")
    parameters("L")
    literal { 45419603L }
}
