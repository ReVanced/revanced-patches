package app.revanced.patches.music.premium.backgroundplay

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.music.misc.backgroundplayback.BackgroundPlaybackPatch

@Deprecated("This patch has been merged into BackgroundPlaybackPatch.")
object BackgroundPlayPatch : BytecodePatch(
    dependencies = setOf(BackgroundPlaybackPatch::class),
) {
    override fun execute(context: BytecodeContext) {
    }
}
