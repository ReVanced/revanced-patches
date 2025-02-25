package app.revanced.patches.youtube.layout.hide.suggestedvideoendscreen

import app.revanced.patcher.patch.bytecodePatch

@Deprecated("Use 'Hide suggested video end screen' instead.")
val disableSuggestedVideoEndScreenPatch = bytecodePatch {
    dependsOn(hideSuggestedVideoEndScreenPatch)
}