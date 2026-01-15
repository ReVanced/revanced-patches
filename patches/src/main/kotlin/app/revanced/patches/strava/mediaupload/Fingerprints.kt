package app.revanced.patches.strava.mediaupload

import app.revanced.patcher.fingerprint

internal val getCompressionQualityFingerprint = fingerprint {
    custom { method, _ ->
        method.name == "getCompressionQuality"
    }
}

internal val getMaxDurationFingerprint = fingerprint {
    custom { method, _ ->
        method.name == "getMaxDuration"
    }
}

internal val getMaxSizeFingerprint = fingerprint {
    custom { method, _ ->
        method.name == "getMaxSize"
    }
}
