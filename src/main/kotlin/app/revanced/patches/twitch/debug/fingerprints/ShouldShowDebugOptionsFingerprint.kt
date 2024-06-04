package app.revanced.patches.twitch.debug.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val shouldShowDebugOptionsFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("/BuildConfigUtil;") && methodDef.name == "shouldShowDebugOptions"
    }
}
