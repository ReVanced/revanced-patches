package app.revanced.patches.twitch.debug.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object IsDebugConfigEnabledFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/BuildConfigUtil;") && methodDef.name == "isDebugConfigEnabled"
    }
)