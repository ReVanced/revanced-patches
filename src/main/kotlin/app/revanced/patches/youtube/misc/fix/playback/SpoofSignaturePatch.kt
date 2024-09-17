package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch

@Deprecated("This patch is obsolete.", replaceWith = ReplaceWith("SpoofVideoStreamsPatch"))
object SpoofSignaturePatch : BytecodePatch(
    dependencies = setOf(SpoofVideoStreamsPatch::class),
) {
    override fun execute(context: BytecodeContext) {}
}

