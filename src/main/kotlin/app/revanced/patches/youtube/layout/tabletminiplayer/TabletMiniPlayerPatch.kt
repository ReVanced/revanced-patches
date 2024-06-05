package app.revanced.patches.youtube.layout.tabletminiplayer

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.layout.miniplayer.MiniPlayerPatch

@Deprecated("This patch class has been renamed to MiniPlayer.")
object TabletMiniPlayerPatch : BytecodePatch(dependencies = setOf(MiniPlayerPatch::class)) {
    override fun execute(context: BytecodeContext) {
    }
}
