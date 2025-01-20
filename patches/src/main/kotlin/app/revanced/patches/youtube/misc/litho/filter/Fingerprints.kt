package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal
import app.revanced.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * In 19.17 and earlier, this resolves to the same method as [readComponentIdentifierFingerprint].
 * In 19.18+ this resolves to a different method.
 */
internal val componentContextParserFingerprint by fingerprint {
    instructions(
        string("TreeNode result must be set."),
        // String changed slightly in 20.03+
        string("it was removed due to duplicate converter bindings.", partialMatch = true)
    )
}

internal val lithoFilterFingerprint by fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    custom { _, classDef ->
        classDef.endsWith("/LithoFilterPatch;")
    }
}

internal val protobufBufferReferenceFingerprint by fingerprint {
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

/**
* In 19.17 and earlier, this resolves to the same method as [componentContextParserFingerprint].
* In 19.18+ this resolves to a different method.
*/
internal val readComponentIdentifierFingerprint by fingerprint {
    instructions(
        string("Number of bits must be positive")
    )
}

internal val emptyComponentFingerprint by fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.CONSTRUCTOR)
    parameters()
    instructions(
        string("EmptyComponent")
    )
    custom { _, classDef ->
        classDef.methods.filter { AccessFlags.STATIC.isSet(it.accessFlags) }.size == 1
    }
}

internal val lithoComponentNameUpbFeatureFlagFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    instructions(
        literal(45631264L )
    )
}

internal val lithoConverterBufferUpbFeatureFlagFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("L")
    parameters("L")
    instructions(
        literal(45419603L)
    )
}
