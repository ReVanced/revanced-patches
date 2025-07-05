package app.revanced.patches.inshorts.ad

import app.revanced.patcher.fingerprint

internal val inshortsAdsFingerprint by fingerprint {
    returns("V")
    strings("GoogleAdLoader", "exception in requestAd")
}