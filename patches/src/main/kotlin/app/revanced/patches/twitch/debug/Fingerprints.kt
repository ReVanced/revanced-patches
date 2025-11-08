package app.revanced.patches.twitch.debug

import app.revanced.patcher.fingerprint

internal val isDebugConfigEnabledFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/BuildConfigUtil;") && method.name == "isDebugConfigEnabled"
    }
}

internal val isOmVerificationEnabledFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/BuildConfigUtil;") && method.name == "isOmVerificationEnabled"
    }
}

internal val shouldShowDebugOptionsFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/BuildConfigUtil;") && method.name == "shouldShowDebugOptions"
    }
}
