package app.revanced.patches.twitch.ad.video.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object CheckAdEligibilityLambdaFingerprint : MethodFingerprint(
    returnType = "Lio/reactivex/Single;",
    parameters = listOf("L"),
    customFingerprint = { method, _ ->
        method.definingClass.endsWith("/AdEligibilityFetcher;")
                && method.name == "shouldRequestAd"
    }
)