package app.revanced.patches.youtube.misc.privacy.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val systemShareSheetFingerprint = methodFingerprint {
    returns("V")
    parameters("L", "Ljava/util/Map;")
    opcodes(
        Opcode.CHECK_CAST,
        Opcode.GOTO,
    )
    strings("YTShare_Logging_Share_Intent_Endpoint_Byte_Array")
}
