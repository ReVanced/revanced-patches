package app.revanced.patches.youtube.layout.hide.albumcards

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.layout.hide.general.HideLayoutComponentsPatch

@Deprecated("This patch has been merged to HideLayoutComponentsPatch.")
@Suppress("unused")
object AlbumCardsPatch : BytecodePatch(
    dependencies = setOf(HideLayoutComponentsPatch::class),
) {
    override fun execute(context: BytecodeContext) { }
}