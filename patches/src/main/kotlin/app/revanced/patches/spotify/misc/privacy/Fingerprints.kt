package app.revanced.patches.spotify.misc.privacy

import app.revanced.patcher.fingerprint

internal val shareUrlToStringFingerprint = fingerprint {
    strings("ShareUrl(url=")
}