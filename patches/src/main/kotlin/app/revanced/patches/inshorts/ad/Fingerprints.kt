package app.revanced.patches.inshorts.ad

import app.revanced.patcher.fingerprint

internal val inshortsAdsFingerprint = fingerprint {
    returns("V")
    strings("GoogleAdLoader", "exception in requestAd")
}