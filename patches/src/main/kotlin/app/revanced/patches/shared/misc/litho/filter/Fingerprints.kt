package app.revanced.patches.shared.misc.litho.filter

import app.revanced.patcher.accessFlags
import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.custom
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.gettingFirstMutableMethod
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.lithoFilterMethod by gettingFirstMutableMethodDeclaratively {
    definingClass { endsWith("/LithoFilterPatch;") }
    accessFlags(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR)
}

/**
 * Matches a method that use the protobuf of our component.
 */
internal val BytecodePatchContext.protobufBufferReferenceLegacyMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("I", "Ljava/nio/ByteBuffer;")
    opcodes(Opcode.IPUT, Opcode.INVOKE_VIRTUAL, Opcode.MOVE_RESULT, Opcode.SUB_INT_2ADDR)
}

internal val BytecodePatchContext.componentContextParserMethodMatch by composingFirstMethod {
    instructions("Number of bits must be positive"())
}

internal val BytecodePatchContext.emptyComponentMethod by gettingFirstMethodDeclaratively("EmptyComponent") {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.CONSTRUCTOR)
    parameterTypes()
    custom {
        immutableClassDef.methods.filter { AccessFlags.STATIC.isSet(it.accessFlags) }.size == 1
    }
}

internal val BytecodePatchContext.componentCreateMethod by gettingFirstMutableMethod(
    "Element missing correct type extension",
    "Element missing type",
)

internal val BytecodePatchContext.lithoThreadExecutorMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameterTypes("I", "I", "I")
    instructions(1L()) // 1L = default thread timeout.
    custom { immutableClassDef.superclass == "Ljava/util/concurrent/ThreadPoolExecutor;" }
}
