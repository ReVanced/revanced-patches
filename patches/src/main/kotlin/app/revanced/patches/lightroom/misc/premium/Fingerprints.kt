package app.revanced.patches.lightroom.misc.premium

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.hasPurchasedMethod by gettingFirstMethodDeclaratively("isPurchaseDoneRecently = true, access platform profile present? = ") {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returnType("Z")
    opcodes(
        Opcode.SGET_OBJECT,
        Opcode.CONST_4,
        Opcode.CONST_4,
        Opcode.CONST_4,
    )
}
