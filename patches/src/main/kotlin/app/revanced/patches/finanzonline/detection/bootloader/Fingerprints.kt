package app.revanced.patches.finanzonline.detection.bootloader

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Located @ at.gv.bmf.bmf2go.taxequalization.tools.utils.AttestationHelper#isBootStateOk (3.0.1)
internal val BytecodePatchContext.bootStateMethod by gettingFirstMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC)
    returnType("Z")
    opcodes(
        Opcode.INVOKE_DIRECT,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.NEW_ARRAY,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.APUT_OBJECT,
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC,
        Opcode.SGET_OBJECT,
        Opcode.IF_EQ,
        Opcode.SGET_OBJECT,
        Opcode.IF_NE,
        Opcode.GOTO,
        Opcode.MOVE,
        Opcode.RETURN,
    )
}

// Located @ at.gv.bmf.bmf2go.taxequalization.tools.utils.AttestationHelper#createKey (3.0.1)
internal val BytecodePatchContext.createKeyMethod by gettingFirstMethodDeclaratively(
    "attestation",
    "SHA-256",
    "random",
    "EC",
    "AndroidKeyStore",
) {
    accessFlags(AccessFlags.PUBLIC)
    returnType("Z")
}
