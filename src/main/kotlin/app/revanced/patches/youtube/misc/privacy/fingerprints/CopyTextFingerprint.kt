package app.revanced.patches.youtube.misc.privacy.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val copyTextFingerprint = methodFingerprint {
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
