package app.revanced.patches.youtube.misc.hapticfeedback

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.markerHapticsMethod by gettingFirstMutableMethodDeclaratively(
    "Failed to execute markers haptics vibrate.",
) {
    returnType("V")
}

internal val BytecodePatchContext.scrubbingHapticsMethod by gettingFirstMutableMethodDeclaratively(
    "Failed to haptics vibrate for fine scrubbing.",
) {
    returnType("V")
}

internal val BytecodePatchContext.seekUndoHapticsMethod by gettingFirstMutableMethodDeclaratively(
    "Failed to execute seek undo haptics vibrate.",
) {
    returnType("V")
}

internal val BytecodePatchContext.zoomHapticsMethod by gettingFirstMutableMethodDeclaratively(
    "Failed to haptics vibrate for video zoom",
) {
    returnType("V")
}
