package app.revanced.patches.reddit.customclients.boostforreddit.fix.downloads

import app.revanced.patcher.fingerprint.methodFingerprint

internal val downloadAudioFingerprint = methodFingerprint {
    strings("/DASH_audio.mp4", "/audio", "v.redd.it", "/")
}
