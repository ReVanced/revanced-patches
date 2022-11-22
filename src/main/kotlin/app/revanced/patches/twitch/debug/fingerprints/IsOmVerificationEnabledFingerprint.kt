package app.revanced.patches.twitch.debug.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object IsOmVerificationEnabledFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("BuildConfigUtil;") && methodDef.name == "isOmVerificationEnabled"
    }
)