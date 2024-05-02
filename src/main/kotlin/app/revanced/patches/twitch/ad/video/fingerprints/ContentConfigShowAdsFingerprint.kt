package app.revanced.patches.twitch.ad.video.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val contentConfigShowAdsFingerprint = methodFingerprint {
    returns("Z")
    parameters()
    custom { method, _ ->
        method.definingClass.endsWith("/ContentConfigData;") && method.name == "getShowAds"
    }
}
