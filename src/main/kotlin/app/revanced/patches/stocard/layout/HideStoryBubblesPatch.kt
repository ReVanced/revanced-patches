package app.revanced.patches.stocard.layout

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.util.getNode

@Patch(
    name = "Hide story bubbles",
    compatiblePackages = [CompatiblePackage("de.stocard.stocard")],
)
@Suppress("unused")
object HideStoryBubblesPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        context.document["res/layout/rv_story_bubbles_list.xml"].use { document ->
            document.getNode("androidx.recyclerview.widget.RecyclerView").apply {
                arrayOf(
                    "android:layout_width",
                    "android:layout_height",
                ).forEach {
                    attributes.getNamedItem(it).nodeValue = "0dp"
                }
            }
        }
    }
}
