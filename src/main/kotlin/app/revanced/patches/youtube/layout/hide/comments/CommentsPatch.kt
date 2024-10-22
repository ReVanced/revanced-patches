package app.revanced.patches.youtube.layout.hide.comments

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patches.youtube.layout.hide.general.HideLayoutComponentsPatch

@Deprecated("This patch has been merged to HideLayoutComponentsPatch.")
@Suppress("unused")
object CommentsPatch : ResourcePatch(
    dependencies = setOf(HideLayoutComponentsPatch::class),
) {
    override fun execute(context: ResourceContext) { }
}