package app.revanced.patches.pandora.misc

import app.revanced.patcher.fingerprint

internal val skipLimitBehaviorFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "getSkipLimitBehavior" && classDef.endsWith("UserData;")
    }
}