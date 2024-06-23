package app.revanced.patches.youtube.misc.minimizedplayback

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.misc.backgroundplayback.BackgroundPlaybackPatch

@Deprecated("This patch class has been renamed to BackgroundPlaybackPatch.")
object MinimizedPlaybackPatch : BytecodePatch(dependencies = setOf(BackgroundPlaybackPatch::class)) {
    override fun execute(context: BytecodeContext) {
    }
}