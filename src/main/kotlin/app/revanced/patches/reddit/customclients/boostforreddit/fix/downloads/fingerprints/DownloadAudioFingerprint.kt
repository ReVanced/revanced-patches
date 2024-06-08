package app.revanced.patches.reddit.customclients.boostforreddit.fix.downloads.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object DownloadAudioFingerprint : MethodFingerprint(
    strings = setOf("/DASH_audio.mp4", "/audio", "v.redd.it", "/"),
)
