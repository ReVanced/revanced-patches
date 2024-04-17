package app.revanced.patches.youtube.layout.hide.breakingnews

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.layout.hide.general.HideLayoutComponentsPatch

@Deprecated("This patch has been merged to HideLayoutComponentsPatch.")
object BreakingNewsPatch : BytecodePatch(
    dependencies = setOf(HideLayoutComponentsPatch::class),
) {
    override fun execute(context: BytecodeContext) {
    }
}
