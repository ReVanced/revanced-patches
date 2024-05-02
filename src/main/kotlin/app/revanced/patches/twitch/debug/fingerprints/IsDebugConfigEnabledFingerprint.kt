package app.revanced.patches.twitch.debug.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val isDebugConfigEnabledFingerprint = methodFingerprint {
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/BuildConfigUtil;") && methodDef.name == "isDebugConfigEnabled"
    }
}
