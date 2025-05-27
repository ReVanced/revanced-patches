package app.revanced.patches.youtube.seekbar.hideseekbar

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.seekbar.seekbarpatch.hideSeekbarPatch

@Deprecated("Patch was moved to app.revanced.patches.youtube.interaction.seekbar")
val hideSeekbarPatch = bytecodePatch {
    dependsOn(
        hideSeekbarPatch
    )
}
