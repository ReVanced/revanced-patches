package app.revanced.patches.twitch.debug.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object ShouldShowDebugOptionsFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/BuildConfigUtil;") && methodDef.name == "shouldShowDebugOptions"
    }
)