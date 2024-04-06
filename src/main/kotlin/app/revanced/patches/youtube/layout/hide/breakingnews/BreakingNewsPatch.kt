package app.revanced.patches.youtube.layout.hide.breakingnews

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.layout.hide.shelves.HideHorizontalShelvesPatch

@Deprecated("This patch class has been renamed to HideHorizontalShelvesPatch.")
object BreakingNewsPatch : BytecodePatch(
    dependencies = setOf(HideHorizontalShelvesPatch::class),
) {
    override fun execute(context: BytecodeContext) {
    }
}
