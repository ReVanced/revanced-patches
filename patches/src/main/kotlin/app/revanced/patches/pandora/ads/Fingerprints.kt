package app.revanced.patches.pandora.ads

import app.revanced.patcher.fingerprint

internal val isAdSupportedFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "getIsAdSupported" && classDef.endsWith("UserData;")
    }
}

internal val requestAudioAdFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "requestAudioAdFromAdSDK" && classDef.endsWith("ContentServiceOpsImpl;")
    }
}