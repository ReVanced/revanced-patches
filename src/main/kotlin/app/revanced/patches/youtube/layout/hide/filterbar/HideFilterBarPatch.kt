package app.revanced.patches.youtube.layout.hide.filterbar

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.layout.hide.general.HideLayoutComponentsPatch

@Deprecated("This patch class has been merged into HideLayoutComponentsPatch.")
object HideFilterBarPatch : BytecodePatch(
    dependencies = setOf(HideLayoutComponentsPatch::class)
) {
    override fun execute(context: BytecodeContext) {}
}