package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.util.containsLiteralInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.componentCreateMethod by gettingFirstMethodDeclaratively {
    instructions(
        "Element missing correct type extension"(),
        "Element missing type"(),
    )
}

internal val BytecodePatchContext.lithoFilterMethod by gettingFirstMethodDeclaratively {
    definingClass { endsWith("/LithoFilterPatch;") }
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
}

internal val BytecodePatchContext.protobufBufferReferenceMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("[B")

    var methodDefiningClass = ""
    custom {
        methodDefiningClass = definingClass
        true
    }

    instructions(
        allOf(
            Opcode.IGET_OBJECT(),
            field { definingClass == methodDefiningClass && type == "Lcom/google/android/libraries/elements/adl/UpbMessage;" },
        ),
        method { definingClass == "Lcom/google/android/libraries/elements/adl/UpbMessage;" && name == "jniDecode" },
    )
}

internal val BytecodePatchContext.protobufBufferReferenceLegacyMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("I", "Ljava/nio/ByteBuffer;")
    opcodes(
        Opcode.IPUT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.SUB_INT_2ADDR,
    )
}

internal val BytecodePatchContext.emptyComponentMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.CONSTRUCTOR)
    parameterTypes()
    instructions("EmptyComponent"())
    custom { immutableClassDef.methods.filter { AccessFlags.STATIC.isSet(it.accessFlags) }.size == 1 }
}

internal val BytecodePatchContext.lithoThreadExecutorMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes("I", "I", "I")
    custom {
        immutableClassDef.superclass == "Ljava/util/concurrent/ThreadPoolExecutor;" &&
            containsLiteralInstruction(1L) // 1L = default thread timeout.
    }
}

internal val BytecodePatchContext.lithoComponentNameUpbFeatureFlagMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(
        45631264L(),
    )
}

internal val BytecodePatchContext.lithoConverterBufferUpbFeatureFlagMethod by gettingFirstMethodDeclaratively {
    returnType("L")
    instructions(
        45419603L(),
    )
}
