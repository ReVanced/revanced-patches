package app.revanced.patches.youtube.layout.hide.loadmorebutton

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.layout.hide.showmorebutton.HideShowMoreButtonPatch

@Deprecated("This patch class has been renamed to HideShowMoreButtonPatch.")
object HideLoadMoreButtonPatch : BytecodePatch(
    dependencies = setOf(HideShowMoreButtonPatch::class)
) {
    override fun execute(context: BytecodeContext) {}
}