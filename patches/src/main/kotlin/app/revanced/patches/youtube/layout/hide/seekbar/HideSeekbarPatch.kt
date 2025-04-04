package app.revanced.patches.youtube.layout.hide.seekbar

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.interaction.seekbar.hideSeekbarPatch

@Deprecated("Patch was moved to app.revanced.patches.youtube.interaction.seekbar")
val hideSeekbarPatch = bytecodePatch {
    dependsOn(
        hideSeekbarPatch
    )
}
