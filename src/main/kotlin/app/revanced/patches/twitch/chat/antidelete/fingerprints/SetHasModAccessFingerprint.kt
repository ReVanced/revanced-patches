package app.revanced.patches.twitch.chat.antidelete.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object SetHasModAccessFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("DeletedMessageClickableSpan;") && methodDef.name == "setHasModAccess"
    }
)