package app.revanced.patches.youtube.layout.tablet

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch

@Deprecated("This patch class has been renamed to TabletLayoutPatch.")
object EnableTabletLayoutPatch : BytecodePatch(dependencies = setOf(TabletLayoutPatch::class)) {
    override fun execute(context: BytecodeContext) {
    }
}
