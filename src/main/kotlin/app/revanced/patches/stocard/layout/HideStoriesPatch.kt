package app.revanced.patches.stocard.layout

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch

@Patch(name = "Hide story bubbles", compatiblePackages = [CompatiblePackage("de.stocard.stocard")])
object HideStoriesPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        context.document["res/layout/rv_story_bubbles_list.xml"].use { document ->
            val view = document.getElementsByTagName("androidx.recyclerview.widget.RecyclerView").item(0)
            view.attributes.getNamedItem("android:layout_width").nodeValue = "0dp"
            view.attributes.getNamedItem("android:layout_height").nodeValue = "0dp"
        }
    }
}