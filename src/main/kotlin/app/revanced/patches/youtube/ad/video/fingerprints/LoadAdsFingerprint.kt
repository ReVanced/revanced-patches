package app.revanced.patches.youtube.ad.video.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.ad.video.annotations.VideoAdsCompatibility

@Name("load-ads-fingerprint")

@VideoAdsCompatibility
@Version("0.0.1")
object LoadAdsFingerprint : MethodFingerprint(
    strings = listOf(
        "OnFulfillmentTriggersActivated has non registered slot",
        "markFillRequested",
        "Trying to enter a slot when a slot of same type and physical position is already active. Its status: ",
    )
)