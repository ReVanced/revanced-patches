package app.revanced.patches.youtube.misc.litho.filter

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

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

internal val readComponentIdentifierFingerprint = fingerprint {
    opcodes(
        Opcode.IF_NEZ,
        null,
        Opcode.MOVE_RESULT_OBJECT, // Register stores the component identifier string
    )
}
