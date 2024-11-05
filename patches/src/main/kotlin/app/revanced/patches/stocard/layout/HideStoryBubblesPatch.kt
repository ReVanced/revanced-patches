package app.revanced.patches.stocard.layout

import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.getNode

@Suppress("unused")
val hideStoryBubblesPatch = resourcePatch(
    name = "Hide story bubbles",
) {
    compatibleWith("de.stocard.stocard")

    execute {
        document("res/layout/rv_story_bubbles_list.xml").use { document ->
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
