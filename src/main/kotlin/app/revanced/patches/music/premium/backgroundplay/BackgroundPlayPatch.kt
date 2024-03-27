package app.revanced.patches.music.premium.backgroundplay

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.music.layout.minimizedplayback.MinimizedPlaybackPatch
@Deprecated("This patch has been merged into MinimizedPlaybackPatch.")
object BackgroundPlayPatch : BytecodePatch(
    dependencies = setOf(MinimizedPlaybackPatch::class),
) {
    override fun execute(context: BytecodeContext) {
    }
}
