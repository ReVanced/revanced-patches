package app.revanced.patches.twitch.ad.video.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object GetReadyToShowAdFingerprint : MethodFingerprint(
    customFingerprint = { method ->
        method.definingClass.endsWith("/StreamDisplayAdsPresenter;") && method.name == "getReadyToShowAdOrAbort"
    }
)