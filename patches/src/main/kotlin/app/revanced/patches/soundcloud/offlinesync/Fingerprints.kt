package app.revanced.patches.soundcloud.offlinesync

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.downloadOperationsURLBuilderMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("Ljava/lang/String")
    parameterTypes("L", "L")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.SGET_OBJECT,
        Opcode.FILLED_NEW_ARRAY,
    )
}

internal val BytecodePatchContext.downloadOperationsHeaderVerificationMethod by gettingFirstMutableMethodDeclaratively(
    "X-SC-Mime-Type",
    "X-SC-Preset",
    "X-SC-Quality",
) {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("L", "L")
    opcodes(
        Opcode.CONST_STRING,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_STRING,
    )
}
