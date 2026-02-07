package app.revanced.patches.shared.misc.litho.filter

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal val BytecodePatchContext.accessibilityIdMethodMatch by composingFirstMethod {
    instructions(
        allOf(
            Opcode.INVOKE_INTERFACE(),
            method { parameterTypes.isEmpty() && returnType == "Ljava/lang/String;" }
        ),
        afterAtMost(5, "primary_image"()),
    )
}

internal fun BytecodePatchContext.getAccessibilityTextMethodMatch(accessibilityIdMethod: MethodReference) = firstMethodComposite {
    returnType("V")
    custom {
        // 'public final synthetic' or 'public final bridge synthetic'.
        AccessFlags.SYNTHETIC.isSet(accessFlags)
    }
    instructions(
        allOf(
            Opcode.INVOKE_INTERFACE(),
            method { parameterTypes.isEmpty() && returnType == "Ljava/lang/String;" }
        ),
        afterAtMost(5, method { this == accessibilityIdMethod })
    )
}
internal val BytecodePatchContext.lithoFilterInitMethod by gettingFirstMethodDeclaratively {
    definingClass("/LithoFilterPatch;")
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
}

internal val BytecodePatchContext.protobufBufferReferenceMethodMatch by composingFirstMethod {
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

/**
 * Matches a method that use the protobuf of our component.
 */
internal val BytecodePatchContext.protobufBufferReferenceLegacyMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("I", "Ljava/nio/ByteBuffer;")
    opcodes(Opcode.IPUT, Opcode.INVOKE_VIRTUAL, Opcode.MOVE_RESULT, Opcode.SUB_INT_2ADDR)
}

internal val BytecodePatchContext.emptyComponentMethod by gettingFirstImmutableMethodDeclaratively {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.CONSTRUCTOR)
    parameterTypes()
    instructions("EmptyComponent"())
    custom { immutableClassDef.methods.filter { AccessFlags.STATIC.isSet(it.accessFlags) }.size == 1 }
}

internal val BytecodePatchContext.componentCreateMethod by gettingFirstMethod(
    "Element missing correct type extension",
    "Element missing type",
)

internal val BytecodePatchContext.lithoThreadExecutorMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes("I", "I", "I")
    instructions(1L()) // 1L = default thread timeout.
    custom { immutableClassDef.superclass == "Ljava/util/concurrent/ThreadPoolExecutor;" }
}
