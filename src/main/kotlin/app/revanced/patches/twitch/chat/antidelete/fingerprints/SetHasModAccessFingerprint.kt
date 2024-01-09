package app.revanced.patches.twitch.chat.antidelete.fingerprints


import app.revanced.patcher.fingerprint.MethodFingerprint

internal object SetHasModAccessFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("DeletedMessageClickableSpan;") && methodDef.name == "setHasModAccess"
    }
)