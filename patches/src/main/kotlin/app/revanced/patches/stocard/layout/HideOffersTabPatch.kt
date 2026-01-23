package app.revanced.patches.stocard.layout

import app.revanced.patcher.patch.creatingResourcePatch
import app.revanced.util.childElementsSequence
import app.revanced.util.getNode

@Suppress("unused")
val `Hide offers tab` by creatingResourcePatch {
    compatibleWith("de.stocard.stocard")

    apply {
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
