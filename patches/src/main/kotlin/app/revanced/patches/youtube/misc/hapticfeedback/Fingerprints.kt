package app.revanced.patches.youtube.misc.hapticfeedback

internal val BytecodePatchContext.markerHapticsMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    strings("Failed to execute markers haptics vibrate.")
}

internal val BytecodePatchContext.scrubbingHapticsMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    strings("Failed to haptics vibrate for fine scrubbing.")
}

internal val BytecodePatchContext.seekUndoHapticsMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    strings("Failed to execute seek undo haptics vibrate.")
}

internal val BytecodePatchContext.zoomHapticsMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    strings("Failed to haptics vibrate for video zoom")
}
