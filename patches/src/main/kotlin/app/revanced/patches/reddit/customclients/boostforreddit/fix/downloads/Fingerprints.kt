package app.revanced.patches.reddit.customclients.boostforreddit.fix.downloads

import app.revanced.patcher.fingerprint

internal val downloadAudioFingerprint = fingerprint {
    strings("/DASH_audio.mp4", "/audio")
}
