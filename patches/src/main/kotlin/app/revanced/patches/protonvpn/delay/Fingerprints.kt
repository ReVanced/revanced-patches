package app.revanced.patches.protonvpn.delay

import app.revanced.patcher.fingerprint


internal val longDelayFingerprint = fingerprint {
    custom { method, _ ->
        method.name == "getChangeServerLongDelayInSeconds"
    }
}

internal val shortDelayFingerprint = fingerprint {
    custom { method, _ ->
        method.name == "getChangeServerShortDelayInSeconds"
    }
}