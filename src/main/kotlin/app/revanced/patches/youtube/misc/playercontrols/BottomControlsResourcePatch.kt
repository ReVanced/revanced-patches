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
    private lateinit var targetXmlEditor: DomFileEditor

    override fun execute(context: ResourceContext) {
        resourceContext = context
        targetXmlEditor = context.xmlEditor[TARGET_RESOURCE]

        bottomUiContainerResourceId = ResourceMappingPatch.resourceMappings
            .single { it.type == "id" && it.name == "bottom_ui_container_stub" }.id
    }

    /**
     * Add new controls to the bottom of the YouTube player.
     *
     * @param resourceDirectoryName The name of the directory containing the hosting resource.
     */
    fun addControls(resourceDirectoryName: String) {
        val sourceXmlEditor = resourceContext.xmlEditor[
            this::class.java.classLoader.getResourceAsStream(
                "$resourceDirectoryName/host/layout/$TARGET_RESOURCE_NAME"
            )!!
        ]

        val targetElement = "android.support.constraint.ConstraintLayout"

        val hostElements = sourceXmlEditor.file.getElementsByTagName(targetElement).item(0).childNodes

        val destinationResourceFile = targetXmlEditor.file
        val destinationElement = destinationResourceFile.getElementsByTagName(targetElement).item(0)

        for (index in 1 until hostElements.length) {
            val element = hostElements.item(index).cloneNode(true)

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
            destinationResourceFile.adoptNode(element)
            destinationElement.appendChild(element)
        }
        sourceXmlEditor.close()
    }

    override fun close() = targetXmlEditor.close()
}
