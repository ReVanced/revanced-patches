package app.revanced.patches.youtube.layout.buttons.captions

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.layout.buttons.player.HidePlayerOverlayButtonsPatch

@Suppress("unused")
@Deprecated("This patch has been merged into HidePlayerOverlayButtonsPatch.")
object HideCaptionsButtonPatch : BytecodePatch(
    dependencies = setOf(HidePlayerOverlayButtonsPatch::class),
) {
    override fun execute(context: BytecodeContext) {
    }
}
