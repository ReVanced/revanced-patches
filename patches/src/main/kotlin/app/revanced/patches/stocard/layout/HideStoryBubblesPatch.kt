package app.revanced.patches.stocard.layout

import app.revanced.patcher.patch.creatingResourcePatch
import app.revanced.util.getNode

@Suppress("unused", "ObjectPropertyName")
val `Hide story bubbles` by creatingResourcePatch {
    compatibleWith("de.stocard.stocard")

    apply {
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
