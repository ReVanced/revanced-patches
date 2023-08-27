package app.revanced.patches.youtubevanced.ad.general.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object ContainsAdFingerprint:MethodFingerprint(
    returnType = "Z",
    parameters = listOf("L", "L"),
    accessFlags = AccessFlags.STATIC or AccessFlags.PUBLIC,
    opcodes = listOf(
        Opcode.CONST_STRING,
        Opcode.INVOKE_INTERFACE,
        Opcode.CONST_STRING,
        Opcode.INVOKE_INTERFACE,
        Opcode.CONST_STRING,
        Opcode.INVOKE_INTERFACE
    ),
    strings = listOf("ads_video_with_context"),
    customFingerprint = { methodDef, _ ->
        methodDef.name == "containsAd" && methodDef.definingClass.endsWith("LithoAdRemoval;")
    }
)