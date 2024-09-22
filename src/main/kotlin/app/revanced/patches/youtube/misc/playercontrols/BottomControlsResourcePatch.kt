package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.DomFileEditor
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.util.findElementByAttributeValue
import org.w3c.dom.Element
import java.io.Closeable

@Patch(dependencies = [ResourceMappingPatch::class])
object BottomControlsResourcePatch : ResourcePatch(), Closeable {
    internal var bottomUiContainerResourceId: Long = -1

    private const val TARGET_RESOURCE_NAME = "youtube_controls_bottom_ui_container.xml"
    private const val TARGET_RESOURCE = "res/layout/$TARGET_RESOURCE_NAME"

    private lateinit var resourceContext: ResourceContext
    private lateinit var targetDocumentEditor: DomFileEditor
    private lateinit var targetElement : Element

    override fun execute(context: ResourceContext) {
        bottomUiContainerResourceId = ResourceMappingPatch["id", "bottom_ui_container_stub"]

        resourceContext = context
        targetDocumentEditor = context.xmlEditor[TARGET_RESOURCE]

        // Add all buttons to an inner layout, to prevent
        // cardboard VR from being inserted into the middle.
        targetElement = targetDocumentEditor.file.createElement("LinearLayout")
        targetElement.setAttribute("android:layoutDirection", "ltr")
        targetElement.setAttribute("android:layout_width", "match_parent")
        targetElement.setAttribute("android:layout_height", "wrap_content")
        targetElement.setAttribute("android:paddingTop", "0dip")
        targetElement.setAttribute("android:paddingBottom", "1dip")
        targetElement.setAttribute("android:orientation", "horizontal")
        targetElement.setAttribute("android:gravity", "center_vertical")

        val bottomContainer = targetDocumentEditor.file.childNodes.findElementByAttributeValue(
            "android:id",
            "@id/bottom_end_container"
        ) ?: throw PatchException("Could not find target element")

        bottomContainer.appendChild(targetElement)
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

            targetDocumentEditor.file.adoptNode(element)
            targetElement.appendChild(element)
        }

        sourceDocumentEditor.close()
    }

    override fun close() = targetDocumentEditor.close()
}
