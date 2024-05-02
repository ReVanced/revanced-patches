package app.revanced.patches.twitch.ad.video.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val checkAdEligibilityLambdaFingerprint = methodFingerprint {
    returns("Lio/reactivex/Single;")
    parameters("L")
    custom { method, _ ->
        method.definingClass.endsWith("/AdEligibilityFetcher;") &&
            method.name == "shouldRequestAd"
    }
}
