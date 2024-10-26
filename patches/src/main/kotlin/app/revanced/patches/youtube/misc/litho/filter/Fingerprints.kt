package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * In 19.17 and earlier, this resolves to the same method as [readComponentIdentifierFingerprint].
 * In 19.18+ this resolves to a different method.
 */
internal val componentContextParserFingerprint = fingerprint {
    strings("Component was not found %s because it was removed due to duplicate converter bindings.")
}

internal val emptyComponentBuilderFingerprint = fingerprint {
    opcodes(
        Opcode.INVOKE_INTERFACE,
        Opcode.INVOKE_STATIC_RANGE,
    )
}

internal val lithoFilterFingerprint = fingerprint {
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
    returns("V")
    custom { _, classDef ->
        classDef.endsWith("LithoFilterPatch;")
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

/**
* In 19.17 and earlier, this resolves to the same method as [componentContextParserFingerprint].
* In 19.18+ this resolves to a different method.
*/
internal val readComponentIdentifierFingerprint = fingerprint {
    strings("Number of bits must be positive")
}

internal val emptyComponentFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.CONSTRUCTOR)
    parameters()
    strings("EmptyComponent")
    custom { _, classDef ->
        classDef.methods.filter { AccessFlags.STATIC.isSet(it.accessFlags) }.size == 1
    }
}
