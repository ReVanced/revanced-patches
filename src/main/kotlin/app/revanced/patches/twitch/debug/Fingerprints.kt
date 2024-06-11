package app.revanced.patches.twitch.debug

import app.revanced.patcher.fingerprint.methodFingerprint

internal val isDebugConfigEnabledFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("/BuildConfigUtil;") && methodDef.name == "isDebugConfigEnabled"
    }
}

internal val isOmVerificationEnabledFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("/BuildConfigUtil;") && methodDef.name == "isOmVerificationEnabled"
    }
}

internal val shouldShowDebugOptionsFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("/BuildConfigUtil;") && methodDef.name == "shouldShowDebugOptions"
    }
}
