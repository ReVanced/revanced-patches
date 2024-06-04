package app.revanced.patches.youtube.layout.tabletminiplayer

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.youtube.layout.tablet.TabletLayoutPatch

@Deprecated("This patch class has been merged into TabletLayoutPatch.")
object TabletMiniPlayerPatch : BytecodePatch(dependencies = setOf(TabletLayoutPatch::class)) {
    override fun execute(context: BytecodeContext) {
    }
}
