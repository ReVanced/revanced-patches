package app.revanced.patches.youtube.layout.hide.breakingnews

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.layout.hide.general.HideLayoutComponentsPatch

@Deprecated("This patch has been merged to HideLayoutComponentsPatch.")
@Suppress("unused")
val breakingNewsPatch = bytecodePatch {
    dependsOn(HideLayoutComponentsPatch)
}
