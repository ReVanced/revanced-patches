package app.revanced.patches.music.layout.castbutton

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.music.layout.buttons.hideButtons

@Deprecated("Patch was moved", ReplaceWith("hideButtons"))
@Suppress("unused")
val hideCastButton = bytecodePatch{
    dependsOn(hideButtons)
}
