package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.DomFileEditor
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.util.findElementByAttributeValueOrThrow
import org.w3c.dom.Node
import java.io.Closeable

@Patch(dependencies = [ResourceMappingPatch::class])
internal object PlayerControlsResourcePatch : ResourcePatch(), Closeable {
    private const val TARGET_RESOURCE_NAME = "youtube_controls_bottom_ui_container.xml"
    private const val TARGET_RESOURCE = "res/layout/$TARGET_RESOURCE_NAME"

    internal var bottomUiContainerResourceId: Long = -1L
    internal var controlsLayoutStub: Long = -1L
    internal var heatseekerViewstub = -1L
    internal var fullscreenButton = -1L

    /**
     * The element to the left of the element being added.
     */
    private var lastLeftOf = "@id/fullscreen_button"
    private lateinit var insertBeforeNode: Node

    private lateinit var resourceContext: ResourceContext
    private lateinit var targetDocumentEditor: DomFileEditor
    private lateinit var targetElement : Node

    override fun execute(context: ResourceContext) {
        bottomUiContainerResourceId = ResourceMappingPatch["id", "bottom_ui_container_stub"]
        controlsLayoutStub = ResourceMappingPatch["id", "controls_layout_stub"]
        heatseekerViewstub = ResourceMappingPatch["id", "heatseeker_viewstub"]
        fullscreenButton = ResourceMappingPatch["id", "fullscreen_button"]

        resourceContext = context
        targetDocumentEditor = context.xmlEditor[TARGET_RESOURCE]

        targetElement = targetDocumentEditor.file.getElementsByTagName(
            "android.support.constraint.ConstraintLayout"
        ).item(0)

        insertBeforeNode = targetDocumentEditor.file.childNodes.findElementByAttributeValueOrThrow(
            "android:inflatedId",
            lastLeftOf
        )
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

        val sourceElements = sourceDocumentEditor.file.getElementsByTagName(
            "android.support.constraint.ConstraintLayout"
        ).item(0).childNodes

        // Copy the patch layout xml into the target layout file.
        for (index in 1 until sourceElements.length) {
            val element = sourceElements.item(index).cloneNode(true)

            // If the element has no attributes there's no point to adding it to the destination.
            if (!element.hasAttributes()) continue

            element.attributes.getNamedItem("yt:layout_constraintRight_toLeftOf").nodeValue = lastLeftOf
            lastLeftOf = element.attributes.getNamedItem("android:id").nodeValue

            targetDocumentEditor.file.adoptNode(element)
            // Elements do not need to be added in the layout order since a layout constraint is used,
            // but in order is easier to make sense of while debugging.
            targetElement.insertBefore(element, insertBeforeNode)
            insertBeforeNode = element
        }

        sourceDocumentEditor.close()
    }

    override fun close() {
        arrayOf(
            "@id/bottom_end_container",
            "@id/multiview_button",
        ).forEach {
            targetDocumentEditor.file.childNodes.findElementByAttributeValueOrThrow(
                "android:id",
                it
            ).setAttribute("yt:layout_constraintRight_toLeftOf", lastLeftOf)
        }

        targetDocumentEditor.close()
    }
}
