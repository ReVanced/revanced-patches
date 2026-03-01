package app.revanced.patches.memegenerator.detection.license

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.licenseValidationMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returnType("Z")
    parameterTypes("Landroid/content/Context;")
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_WIDE,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_WIDE,
        Opcode.CMP_LONG,
        Opcode.IF_GEZ,
        Opcode.CONST_4,
        Opcode.RETURN,
        Opcode.CONST_4,
        Opcode.RETURN
    )
}