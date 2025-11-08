package app.revanced.patches.youtube.misc.hapticfeedback

import app.revanced.patcher.fingerprint

internal val markerHapticsFingerprint = fingerprint {
    returns("V")
    strings("Failed to execute markers haptics vibrate.")
}

internal val scrubbingHapticsFingerprint = fingerprint {
    returns("V")
    strings("Failed to haptics vibrate for fine scrubbing.")
}

internal val seekUndoHapticsFingerprint = fingerprint {
    returns("V")
    strings("Failed to execute seek undo haptics vibrate.")
}

internal val zoomHapticsFingerprint = fingerprint {
    returns("V")
    strings("Failed to haptics vibrate for video zoom")
}
