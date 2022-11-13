package app.revanced.patches.twitch.ad.video.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.twitch.ad.video.annotations.VideoAdsCompatibility
import org.jf.dexlib2.AccessFlags

@Name("check-ad-eligibility-fingerprint")
@VideoAdsCompatibility
@Version("0.0.1")
object CheckAdEligibilityLambdaFingerprint : MethodFingerprint(
    "L",
    AccessFlags.PRIVATE or AccessFlags.FINAL or AccessFlags.STATIC,
    listOf("L", "L", "L"),
    customFingerprint = { method ->
        method.definingClass.endsWith("AdEligibilityFetcher;") &&
                method.name.contains("shouldRequestAd")
    }
)