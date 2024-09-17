package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch

@Deprecated("This patch will be removed in the future.")
object SpoofSignatureResourcePatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {}
}
