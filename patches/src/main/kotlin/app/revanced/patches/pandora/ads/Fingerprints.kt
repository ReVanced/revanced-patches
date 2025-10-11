package app.revanced.patches.pandora.ads

import app.revanced.patcher.fingerprint

internal val getIsAdSupportedFingerprint by fingerprint {
    custom { method, classDef ->
        method.name == "getIsAdSupported" && classDef.endsWith("UserData;")
    }
}

internal val requestAudioAdFingerprint by fingerprint {
    custom { method, classDef ->
        method.name == "requestAudioAdFromAdSDK" && classDef.endsWith("ContentServiceOpsImpl;")
    }
}