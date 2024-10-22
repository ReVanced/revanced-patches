package app.revanced.patches.youtube.layout.buttons.autoplay

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.layout.buttons.overlay.HidePlayerOverlayButtonsPatch

@Suppress("unused")
@Deprecated("This patch has been merged into HidePlayerOverlayButtonsPatch.")
object HideAutoplayButtonPatch : BytecodePatch(
    dependencies = setOf(HidePlayerOverlayButtonsPatch::class),
) {
    override fun execute(context: BytecodeContext) {
    }
}