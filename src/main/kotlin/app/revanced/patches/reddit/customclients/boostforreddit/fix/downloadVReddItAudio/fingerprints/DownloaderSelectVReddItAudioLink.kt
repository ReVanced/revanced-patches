package app.revanced.patches.reddit.customclients.boostforreddit.fix.downloadVReddItAudio.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object DownloaderSelectVReddItAudioLink : MethodFingerprint(
    strings = setOf("v.redd.it", "/", "/DASH_audio.mp4", "/audio")
)