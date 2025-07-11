package app.revanced.patches.youtube.misc.hapticfeedback

import app.revanced.patcher.fingerprint

internal val markerHapticsFingerprint by fingerprint {
    returns("V")
    strings("Failed to execute markers haptics vibrate.")
}

internal val scrubbingHapticsFingerprint by fingerprint {
    returns("V")
    strings("Failed to haptics vibrate for fine scrubbing.")
}

internal val seekUndoHapticsFingerprint by fingerprint {
    returns("V")
    strings("Failed to execute seek undo haptics vibrate.")
}

internal val zoomHapticsFingerprint by fingerprint {
    returns("V")
    strings("Failed to haptics vibrate for video zoom")
}
