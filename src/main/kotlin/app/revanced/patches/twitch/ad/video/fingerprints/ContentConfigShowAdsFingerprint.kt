package app.revanced.patches.twitch.ad.video.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object ContentConfigShowAdsFingerprint : MethodFingerprint(
    returnType = "Z",
    parameters = listOf(),
    customFingerprint = { method, _ ->
        method.definingClass.endsWith("/ContentConfigData;") && method.name == "getShowAds"
    }
)