package app.revanced.patches.twitch.debug

import app.revanced.patcher.fingerprint

internal val isDebugConfigEnabledFingerprint by fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/BuildConfigUtil;") && method.name == "isDebugConfigEnabled"
    }
}

internal val isOmVerificationEnabledFingerprint by fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/BuildConfigUtil;") && method.name == "isOmVerificationEnabled"
    }
}

internal val shouldShowDebugOptionsFingerprint by fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/BuildConfigUtil;") && method.name == "shouldShowDebugOptions"
    }
}
