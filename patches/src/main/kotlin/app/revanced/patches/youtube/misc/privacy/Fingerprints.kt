package app.revanced.patches.youtube.misc.privacy

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val copyTextFingerprint by fingerprint {
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

internal val systemShareSheetFingerprint by fingerprint {
    returns("V")
    parameters("L", "Ljava/util/Map;")
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.GOTO,
    )
    strings("YTShare_Logging_Share_Intent_Endpoint_Byte_Array")
}

internal val youtubeShareSheetFingerprint by fingerprint {
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
