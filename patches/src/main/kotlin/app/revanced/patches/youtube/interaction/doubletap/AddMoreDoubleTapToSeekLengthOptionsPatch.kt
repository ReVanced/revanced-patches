package app.revanced.patches.youtube.interaction.doubletap

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.util.findElementByAttributeValueOrThrow
import app.revanced.util.removeFromParent
import org.w3c.dom.Element

@Suppress("unused")
val addMoreDoubleTapToSeekLengthOptionsPatch = resourcePatch(
    name = "Add more double tap to seek length options",
) {
    dependsOn(
        sharedExtensionPatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45"
        )
    )

    execute {
        // Values are hard coded to keep patching simple.
        val doubleTapLengthOptionsString = "3, 5, 10, 15, 20, 30, 60, 120, 180, 240"

        val doubleTapLengths = doubleTapLengthOptionsString
            .replace(" ", "")
            .split(",")
        if (doubleTapLengths.isEmpty()) throw PatchException("Invalid double-tap length elements")

        document("res/values/arrays.xml").use { document ->
            fun Element.removeAllChildren() {
                val children = childNodes // Calling childNodes creates a new list.
                for (i in children.length - 1 downTo 0) {
                    children.item(i).removeFromParent()
                }
            }

            val values = document.childNodes.findElementByAttributeValueOrThrow(
                attributeName = "name",
                value = "double_tap_length_values"
            )
            values.removeAllChildren()

            val entries = document.childNodes.findElementByAttributeValueOrThrow(
                attributeName = "name",
                value = "double_tap_length_entries"
            )
            entries.removeAllChildren()

            doubleTapLengths.forEach { length ->
                val item = document.createElement("item")
                item.textContent = length
                entries.appendChild(item)
                values.appendChild(item.cloneNode(true))
            }
        }
    }
}
