package app.revanced.patches.stocard.layout

import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.childElementsSequence
import app.revanced.util.getNode

@Suppress("unused")
val hideOffersTabPatch = resourcePatch(
    name = "Hide offers tab",
) {
    compatibleWith("de.stocard.stocard")

    execute {
        document("res/menu/bottom_navigation_menu.xml").use { document ->
            document.getNode("menu").apply {
                removeChild(
                    childElementsSequence().first {
                        it.attributes.getNamedItem("android:id")?.nodeValue?.contains("offer") == true
                    },
                )
            }
        }
    }
}
