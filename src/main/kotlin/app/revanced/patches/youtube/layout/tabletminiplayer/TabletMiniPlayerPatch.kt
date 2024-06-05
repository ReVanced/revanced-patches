package app.revanced.patches.youtube.layout.tabletminiplayer

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.layout.miniplayer.MiniplayerPatch

@Deprecated("This patch class has been renamed to Miniplayer.")
object TabletMiniPlayerPatch : BytecodePatch(dependencies = setOf(MiniplayerPatch::class)) {
    override fun execute(context: BytecodeContext) {
    }
}
