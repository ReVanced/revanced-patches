package app.revanced.patches.pandora.misc.hook

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val jsonHookPatchFingerprint = fingerprint {
    opcodes(
        Opcode.INVOKE_INTERFACE, // Add dummy hook to hooks list.
        // Add hooks to the hooks list.
        Opcode.INVOKE_STATIC, // Call buildList.
    )
    custom { method, _ -> method.name == "<clinit>" }
}

internal val userAuthenticationFingerprint = fingerprint {
    parameters(
        "Lorg/json/JSONObject;",
        "Z",
        "Z",
        "I",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Z",
        "Ljava/lang/String;"
    )
    returns("V")
    strings(
        "pandoraOneRenewalUrl",
        "slapViewVideoActivityTitleTextLine2",
        "slapViewVideoActivityTitleText"
    )
}