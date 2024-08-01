package app.revanced.patches.youtube.layout.hide.breakingnews

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.layout.hide.general.hideLayoutComponentsPatch

@Deprecated("This patch has been merged to HideLayoutComponentsPatch.")
@Suppress("unused")
val breakingNewsPatch = bytecodePatch {
    dependsOn(hideLayoutComponentsPatch)
}
