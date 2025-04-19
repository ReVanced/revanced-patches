package app.revanced.patches.spotify.misc.privacy

import app.revanced.patcher.fingerprint

internal val shareUrlToStringFingerprint = fingerprint {
    strings("ShareUrl", "shareId")
    custom { method, _ ->
        method.name == "toString"
    }
}

internal val shareUrlConstructorFingerprint = fingerprint {
    custom { method, _ ->
        method.name == "<init>"
    }
}