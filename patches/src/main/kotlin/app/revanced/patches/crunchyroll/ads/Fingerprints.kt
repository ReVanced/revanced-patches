package app.revanced.patches.crunchyroll.ads

import app.revanced.patcher.fingerprint

internal val videoUrlReadyToStringFingerprint = fingerprint {
    strings("VideoUrlReady(url=", ", enableAds=")
}
