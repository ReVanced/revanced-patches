package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch

@Deprecated("This patch is obsolete.", replaceWith = ReplaceWith("SpoofClientPatch"))
object SpoofSignaturePatch : BytecodePatch(
    dependencies = setOf(SpoofClientPatch::class),
) {
    override fun execute(context: BytecodeContext) {}
}

