package app.revanced.patches.shared.misc.privacy

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val youTubeCopyTextFingerprint = fingerprint {
    returns("V")
    parameters("L", "Ljava/util/Map;")
    opcodes(
        Opcode.IGET_OBJECT, // Contains the text to copy to be sanitized.
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC, // ClipData.newPlainText
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.RETURN_VOID,
    )
    strings("text/plain")
}

internal val youTubeSystemShareSheetFingerprint = fingerprint {
    returns("V")
    parameters("L", "Ljava/util/Map;")
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.GOTO,
    )
    strings("YTShare_Logging_Share_Intent_Endpoint_Byte_Array")
}

internal val youTubeShareSheetFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "Ljava/util/Map;")
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.GOTO,
        Opcode.MOVE_OBJECT,
        Opcode.INVOKE_VIRTUAL,
    )
}
