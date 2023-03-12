package app.revanced.patches.youtubevanced.ad.general.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object ContainsAdFingerprint:MethodFingerprint(
    returnType = "Z",
    parameters = listOf("L", "L"),
    access = AccessFlags.STATIC or AccessFlags.PUBLIC,
    opcodes = listOf(
        Opcode.CONST_STRING,
        Opcode.INVOKE_INTERFACE,
        Opcode.CONST_STRING,
        Opcode.INVOKE_INTERFACE,
        Opcode.CONST_STRING,
        Opcode.INVOKE_INTERFACE
    ),
    strings = listOf("ads_video_with_context"),
    customFingerprint = { methodDef ->
        methodDef.name == "containsAd" && methodDef.definingClass.endsWith("LithoAdRemoval;")
    }
) {
}