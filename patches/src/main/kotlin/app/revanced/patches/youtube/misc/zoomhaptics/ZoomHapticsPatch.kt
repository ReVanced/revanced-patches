package app.revanced.patches.youtube.misc.zoomhaptics

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.hapticfeedback.disableHapticFeedbackPatch

@Deprecated("Superseded by disableHapticFeedbackPatch", ReplaceWith("disableHapticFeedbackPatch"))
val zoomHapticsPatch = bytecodePatch(
    description = "Adds an option to disable haptics when zooming.",
) {
    dependsOn(disableHapticFeedbackPatch)
}