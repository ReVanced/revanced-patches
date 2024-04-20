package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.DomFileEditor
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import java.io.Closeable

@Patch(dependencies = [ResourceMappingPatch::class])
object BottomControlsResourcePatch : ResourcePatch(), Closeable {
    internal var bottomUiContainerResourceId: Long = -1

    private const val TARGET_RESOURCE_NAME = "youtube_controls_bottom_ui_container.xml"
    private const val TARGET_RESOURCE = "res/layout/$TARGET_RESOURCE_NAME"

    // The element to the left of the element being added.
    private var lastLeftOf = "fullscreen_button"

    private lateinit var resourceContext: ResourceContext
    private lateinit var targetDocumentEditor: DomFileEditor

    override fun execute(context: ResourceContext) {
        resourceContext = context
        targetDocumentEditor = context.xmlEditor[TARGET_RESOURCE]

        bottomUiContainerResourceId = ResourceMappingPatch["id", "bottom_ui_container_stub"]
    }

    /**
     * Add new controls to the bottom of the YouTube player.
     *
     * @param resourceDirectoryName The name of the directory containing the hosting resource.
     */
    fun addControls(resourceDirectoryName: String) {
        val sourceDocumentEditor = resourceContext.xmlEditor[
            this::class.java.classLoader.getResourceAsStream(
                "$resourceDirectoryName/host/layout/$TARGET_RESOURCE_NAME",
            )!!,
        ]
        val sourceDocument = sourceDocumentEditor.file
        val targetDocument = targetDocumentEditor.file

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
        sourceDocumentEditor.close()
    }

    override fun close() = targetDocumentEditor.close()
}
