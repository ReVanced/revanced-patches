package app.revanced.patches.youtube.ad.video.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.ad.video.annotations.VideoAdsCompatibility

@Name("load-ads-fingerprint")

@VideoAdsCompatibility
@Version("0.0.1")
object LoadAdsFingerprint : MethodFingerprint(
    strings = listOf("Unexpected playerAd type: "),
    customFingerprint = { method ->
        method.parameterTypes.size > 0 && method.parameterTypes.first() == "Ljava/lang/String;"
    }
)