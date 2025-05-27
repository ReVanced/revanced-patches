package app.revanced.patches.youtube.player.zoomhaptics

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.player.hapticfeedback.disableHapticFeedbackPatch

@Deprecated("Superseded by disableHapticFeedbackPatch", ReplaceWith("disableHapticFeedbackPatch"))
val zoomHapticsPatch = bytecodePatch(
    description = "Adds an option to disable haptics when zooming.",
) {
    dependsOn(disableHapticFeedbackPatch)
}
