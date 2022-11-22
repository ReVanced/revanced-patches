package app.revanced.patches.twitch.ad.video.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags

object CheckAdEligibilityLambdaFingerprint : MethodFingerprint(
    "L",
    AccessFlags.PRIVATE or AccessFlags.FINAL or AccessFlags.STATIC,
    listOf("L", "L", "L"),
    customFingerprint = { method ->
        method.definingClass.endsWith("AdEligibilityFetcher;") &&
                method.name.contains("shouldRequestAd")
    }
)