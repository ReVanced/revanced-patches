package app.revanced.patches.twitch.ad.video.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.twitch.ad.video.annotations.VideoAdsCompatibility

@Name("ads-manager-play-fingerprint")
@VideoAdsCompatibility
@Version("0.0.1")
object AdsManagerFingerprint : MethodFingerprint(
    customFingerprint = { method ->
        method.definingClass.endsWith("AdsManagerImpl;") && method.name == "playAds"
    }
)