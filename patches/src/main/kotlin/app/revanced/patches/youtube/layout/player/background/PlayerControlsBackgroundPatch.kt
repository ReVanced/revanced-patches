package app.revanced.patches.youtube.layout.player.background

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.youtube.layout.buttons.overlay.hidePlayerOverlayButtonsPatch

@Suppress("unused")
@Deprecated("Functionality added to hidePlayerOverlayButtonsPatch", ReplaceWith("hidePlayerOverlayButtonsPatch"))
val playerControlsBackgroundPatch = resourcePatch(
    description = "Removes the dark background surrounding the video player control buttons.",
) {
    dependsOn(hidePlayerOverlayButtonsPatch)
}
