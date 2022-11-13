package app.revanced.patches.twitch.ad.audio.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.twitch.ad.audio.annotations.AudioAdsCompatibility

@Name("audio-ads-presenter-play-fingerprint")
@AudioAdsCompatibility
@Version("0.0.1")
object AudioAdsPresenterPlayFingerprint : MethodFingerprint(
    customFingerprint = { method ->
        method.definingClass.endsWith("AudioAdsPlayerPresenter;") && method.name == "playAd"
    }
)