package app.revanced.patches.youtube.misc.hapticfeedback

import app.revanced.patcher.fingerprint

internal val markerHapticsFingerprint = fingerprint {
    returnType("V")
    strings("Failed to execute markers haptics vibrate.")
}

internal val scrubbingHapticsFingerprint = fingerprint {
    returnType("V")
    strings("Failed to haptics vibrate for fine scrubbing.")
}

internal val seekUndoHapticsFingerprint = fingerprint {
    returnType("V")
    strings("Failed to execute seek undo haptics vibrate.")
}

internal val zoomHapticsFingerprint = fingerprint {
    returnType("V")
    strings("Failed to haptics vibrate for video zoom")
}
