package app.revanced.patches.youtube.misc.privacy.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object SystemShareSheetFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("L", "Ljava/util/Map;"),
    opcodes = listOf(
        Opcode.CHECK_CAST,
        Opcode.GOTO
    ),
    strings = listOf("YTShare_Logging_Share_Intent_Endpoint_Byte_Array")
)