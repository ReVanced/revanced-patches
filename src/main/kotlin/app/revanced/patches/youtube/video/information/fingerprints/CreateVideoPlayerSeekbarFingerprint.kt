package app.revanced.patches.youtube.video.information.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val createVideoPlayerSeekbarFingerprint = methodFingerprint {
    returns("V")
    strings("timed_markers_width")
}
