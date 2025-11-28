package app.revanced.patches.protonvpn.delay

import app.revanced.patcher.fingerprint


internal val longDelayFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "getChangeServerLongDelayInSeconds" &&
            classDef.endsWith("AppConfigResponse;")
    }
}

internal val shortDelayFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "getChangeServerShortDelayInSeconds" &&
            classDef.endsWith("AppConfigResponse;")
    }
}