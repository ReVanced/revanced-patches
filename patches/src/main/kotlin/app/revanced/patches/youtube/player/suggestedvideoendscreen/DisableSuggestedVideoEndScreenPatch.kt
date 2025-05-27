package app.revanced.patches.youtube.player.suggestedvideoendscreen

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.player.endscreensuggestion.hideEndScreenSuggestedVideoPatch

@Deprecated("Use 'Hide suggested video end screen' instead.")
val disableSuggestedVideoEndScreenPatch = bytecodePatch {
    dependsOn(hideEndScreenSuggestedVideoPatch)
}
