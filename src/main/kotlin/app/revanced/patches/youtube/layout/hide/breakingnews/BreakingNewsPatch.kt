package app.revanced.patches.youtube.layout.hide.breakingnews

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.layout.hide.suggestionsshelves.HideSuggestionsShelvesPatch

@Deprecated("This patch class has been renamed to HideSuggestionsShelvesPatch.")
object BreakingNewsPatch : BytecodePatch(
    dependencies = setOf(HideSuggestionsShelvesPatch::class),
) {
    override fun execute(context: BytecodeContext) {
    }
}
