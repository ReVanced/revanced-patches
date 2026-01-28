package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.componentCreateMethod by gettingFirstMutableMethodDeclaratively {
    instructions(
        "Element missing correct type extension"(),
        "Element missing type"(),
    )
}

internal val BytecodePatchContext.lithoFilterMethod by gettingFirstMutableMethodDeclaratively {
    definingClass { endsWith("/LithoFilterPatch;") }
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
}

internal val protobufBufferReferenceMethodMatch = firstMethodComposite {
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

internal val BytecodePatchContext.protobufBufferReferenceLegacyMethod by gettingFirstMutableMethodDeclaratively {
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

internal val BytecodePatchContext.lithoThreadExecutorMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes("I", "I", "I")
    instructions(1L()) // 1L = default thread timeout.
    custom {
        immutableClassDef.superclass == "Ljava/util/concurrent/ThreadPoolExecutor;"
    }
}

internal val BytecodePatchContext.lithoComponentNameUpbFeatureFlagMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Z")
    parameterTypes()
    instructions(45631264L())
}

internal val lithoConverterBufferUpbFeatureFlagMethodMatch = firstMethodComposite {
    returnType("L")
    instructions(45419603L())
}
