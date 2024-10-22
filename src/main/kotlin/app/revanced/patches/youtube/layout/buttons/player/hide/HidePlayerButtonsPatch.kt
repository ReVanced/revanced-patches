package app.revanced.patches.youtube.layout.buttons.player.hide

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.layout.buttons.player.HidePlayerOverlayButtonsPatch

@Deprecated("This patch has been merged into HidePlayerOverlayButtonsPatch.")
object HidePlayerButtonsPatch : BytecodePatch(
    dependencies = setOf(HidePlayerOverlayButtonsPatch::class),
) {
    override fun execute(context: BytecodeContext) {
    }
}
