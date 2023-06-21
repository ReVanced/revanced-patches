package app.revanced.patches.twitch.ad.video.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object GetReadyToShowAdFingerprint : MethodFingerprint(
    returnType = "Ltv/twitch/android/core/mvp/presenter/StateAndAction;",
    parameters = listOf("L", "L"),
    customFingerprint = { method, _ ->
        method.definingClass.endsWith("/StreamDisplayAdsPresenter;") && method.name == "getReadyToShowAdOrAbort"
    }
)