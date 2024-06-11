package app.revanced.patches.inshorts.ad

import app.revanced.patcher.fingerprint.methodFingerprint

internal val inshortsAdsFingerprint = methodFingerprint {
    returns("V")
    strings("GoogleAdLoader", "exception in requestAd")
}