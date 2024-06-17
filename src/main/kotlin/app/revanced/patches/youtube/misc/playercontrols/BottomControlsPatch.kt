package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.Document
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings

/**
 * Add new controls to the bottom of the YouTube player.
 *
 * @param resourceDirectoryName The name of the directory containing the hosting resource.
 */
@Suppress("KDocUnresolvedReference")
lateinit var addBottomControls: (resourceDirectoryName: String) -> Unit
    private set

internal var bottomUiContainerResourceId = -1L
    private set

val bottomControlsPatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    lateinit var targetDocument: Document

    // The element to the left of the element being added.
    var lastLeftOf = "fullscreen_button"

    execute { context ->
        bottomUiContainerResourceId = resourceMappings["id", "bottom_ui_container_stub"]

        val targetResourceName = "youtube_controls_bottom_ui_container.xml"
        targetDocument = context.document["res/layout/$targetResourceName"]

        addBottomControls = { resourceDirectoryName ->
            val sourceDocument = context.document[
                object {}::class.java.classLoader.getResourceAsStream(
                    "$resourceDirectoryName/host/layout/$targetResourceName",
                )!!,
            ]

            val targetElementTag = "android.support.constraint.ConstraintLayout"

            val sourceElements = sourceDocument.getElementsByTagName(targetElementTag).item(0).childNodes
            val targetElement = targetDocument.getElementsByTagName(targetElementTag).item(0)

            for (index in 1 until sourceElements.length) {
                val element = sourceElements.item(index).cloneNode(true)

                // If the element has no attributes there's no point to adding it to the destination.
                if (!element.hasAttributes()) continue

                // Set the elements lastLeftOf attribute to the lastLeftOf value.
                val namespace = "@+id"
                element.attributes.getNamedItem("yt:layout_constraintRight_toLeftOf").nodeValue =
                    "$namespace/$lastLeftOf"

                // Set lastLeftOf attribute to the current element.
                val nameSpaceLength = 5
                lastLeftOf = element.attributes.getNamedItem("android:id").nodeValue.substring(nameSpaceLength)

                // Add the element.
                targetDocument.adoptNode(element)
                targetElement.appendChild(element)
            }

            sourceDocument.close()
        }
    }

    finalize {
        targetDocument.close()
    }
}
