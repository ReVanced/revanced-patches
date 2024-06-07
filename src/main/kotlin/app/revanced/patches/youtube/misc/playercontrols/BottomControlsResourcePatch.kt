package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.DomFileEditor
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.Closeable

@Patch(dependencies = [ResourceMappingPatch::class])
object BottomControlsResourcePatch : ResourcePatch(), Closeable {
    internal var bottomUiContainerResourceId: Long = -1

    private const val TARGET_RESOURCE_NAME = "youtube_controls_bottom_ui_container.xml"
    private const val TARGET_RESOURCE = "res/layout/$TARGET_RESOURCE_NAME"
    private const val FULLSCREEN_BUTTON_LAYOUT = "res/layout/youtube_controls_fullscreen_button.xml"
    private const val CF_FULLSCREEN_BUTTON_LAYOUT = "res/layout/youtube_controls_cf_fullscreen_button.xml"

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

        // Function to update attributes for elements based on a condition
        fun updateAttributesForElement(element: Element, attributes: Map<String, String>) {
            for ((key, value) in attributes) {
                element.setAttribute(key, value)
            }
        }

        // Attributes for bottom buttons
        val buttonsAttributes = mapOf(
            "android:layout_marginBottom" to "6.0dip",
            "android:paddingLeft" to "0.0dip",
            "android:paddingRight" to "0.0dip",
            "android:paddingBottom" to "22.0dip",
            "android:layout_height" to "48.0dip",
            "android:layout_width" to "48.0dip"
        )

        // Attributes for youtube_controls_fullscreen_button_stub
        val stubAttributes = mapOf(
            "android:layout_marginBottom" to "6.0dip",
            "android:layout_height" to "48.0dip",
            "android:layout_width" to "48.0dip"
        )

        // Update attributes to fullscreen_button and other custom buttons
        // Function to process a document and update attributes
        fun processDocument(document: Document, attributes: Map<String, String>) {
            val allElements = document.getElementsByTagName("*")
            for (i in 0 until allElements.length) {
                val element = allElements.item(i) as Element
                val idAttribute = element.attributes.getNamedItem("android:id")
                if (idAttribute != null && idAttribute.nodeValue.endsWith("_button")) {
                    updateAttributesForElement(element, attributes)
                }
            }
        }

        // Update attributes for id/youtube_controls_fullscreen_button_stub
        val stubElement = targetDocument.getElementsByTagName("*").let { elements ->
            (0 until elements.length)
                .map { elements.item(it) as Element }
                .firstOrNull { it.getAttribute("android:id") == "@id/youtube_controls_fullscreen_button_stub" }
        }

        if (stubElement != null) {
            updateAttributesForElement(stubElement, stubAttributes)
        }

        // Update button attributes in youtube_controls_bottom_ui_container.xml
        processDocument(targetDocument, buttonsAttributes)

        // In YouTube 19.09+ fullscreen_button moved to stub layouts
        // Function to update attributes for fullscreen button layouts
        fun updateAttributesForLayout(layoutName: String, attributes: Map<String, String>, resourceContext: ResourceContext) {
            if (resourceContext[layoutName].exists()) {
                val documentEditor = resourceContext.xmlEditor[layoutName]
                processDocument(documentEditor.file, attributes)
                documentEditor.close()
            }
        }

        // Update button attributes in youtube_controls_fullscreen_button.xml
        updateAttributesForLayout(FULLSCREEN_BUTTON_LAYOUT, buttonsAttributes, resourceContext)

        // Update button attributes in youtube_controls_cf_fullscreen_button.xml
        updateAttributesForLayout(CF_FULLSCREEN_BUTTON_LAYOUT, buttonsAttributes, resourceContext)

        sourceDocumentEditor.close()
    }

    override fun close() = targetDocumentEditor.close()
}