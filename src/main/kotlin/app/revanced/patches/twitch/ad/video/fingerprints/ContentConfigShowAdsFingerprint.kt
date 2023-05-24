package app.revanced.patches.twitch.ad.video.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object ContentConfigShowAdsFingerprint : MethodFingerprint(
    customFingerprint = { method, _ ->
        method.definingClass.endsWith("ContentConfigData;") && method.name == "getShowAds"
    }
)