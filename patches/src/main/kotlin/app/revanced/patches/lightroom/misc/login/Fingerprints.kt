package app.revanced.patches.lightroom.misc.login

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.isLoggedInMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
    returnType("Z")
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.SGET_OBJECT,
        Opcode.IF_NE,
        Opcode.CONST_4,
        Opcode.GOTO
    )
}
