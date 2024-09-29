package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.DomFileEditor
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.util.copyXmlNode
import app.revanced.util.findElementByAttributeValue
import app.revanced.util.findElementByAttributeValueOrThrow
import app.revanced.util.inputStreamFromBundledResource
import org.w3c.dom.Node
import java.io.Closeable

@Patch(dependencies = [ResourceMappingPatch::class])
object PlayerControlsResourcePatch : ResourcePatch(), Closeable {
    private const val TARGET_RESOURCE_NAME = "youtube_controls_bottom_ui_container.xml"
    private const val TARGET_RESOURCE = "res/layout/$TARGET_RESOURCE_NAME"

    internal var bottomUiContainerResourceId: Long = -1L
    internal var controlsLayoutStub: Long = -1L
    internal var heatseekerViewstub = -1L
    internal var fullscreenButton = -1L

    private lateinit var resourceContext: ResourceContext

    /**
     * The element to the left of the element being added.
     */
    private var bottomLastLeftOf = "@id/fullscreen_button"
    private lateinit var bottomInsertBeforeNode: Node
    private lateinit var bottomTargetDocumentEditor: DomFileEditor
    private lateinit var bottomTargetElement : Node

    override fun execute(context: ResourceContext) {
        bottomUiContainerResourceId = ResourceMappingPatch["id", "bottom_ui_container_stub"]
        controlsLayoutStub = ResourceMappingPatch["id", "controls_layout_stub"]
        heatseekerViewstub = ResourceMappingPatch["id", "heatseeker_viewstub"]
        fullscreenButton = ResourceMappingPatch["id", "fullscreen_button"]

        resourceContext = context
        bottomTargetDocumentEditor = context.xmlEditor[TARGET_RESOURCE]
        val document = bottomTargetDocumentEditor.file

        bottomTargetElement = document.getElementsByTagName(
            "android.support.constraint.ConstraintLayout"
        ).item(0)

        bottomInsertBeforeNode = document.childNodes.findElementByAttributeValue(
            "android:inflatedId",
            bottomLastLeftOf
        ) ?: document.childNodes.findElementByAttributeValueOrThrow(
            "android:id", // Older targets use non inflated id.
            bottomLastLeftOf
        )
    }

    // Internal until this is modified to work with any patch (and not just SponsorBlock).
    internal fun addTopControls(resourceDirectoryName: String) {
        val hostingResourceStream = inputStreamFromBundledResource(
            resourceDirectoryName,
            "host/layout/youtube_controls_layout.xml",
        )!!

        val editor = resourceContext.xmlEditor["res/layout/youtube_controls_layout.xml"]

        "RelativeLayout".copyXmlNode(
            resourceContext.xmlEditor[hostingResourceStream],
            editor,
        ).use {
            val element = editor.file.childNodes.findElementByAttributeValueOrThrow(
                "android:id",
                "@id/player_video_heading"
            )

            // FIXME: This uses hard coded values that only works with SponsorBlock.
            // If other top buttons are added by other patches, this code must be changed.
            // voting button id from the voting button view from the youtube_controls_layout.xml host file
            val votingButtonId = "@+id/revanced_sb_voting_button"
            element.attributes.getNamedItem("android:layout_toStartOf").nodeValue = votingButtonId
        }
    }

    /**
     * Add new controls to the bottom of the YouTube player.
     *
     * @param resourceDirectoryName The name of the directory containing the hosting resource.
     */
    fun addBottomControls(resourceDirectoryName: String) {
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

            element.attributes.getNamedItem("yt:layout_constraintRight_toLeftOf").nodeValue = bottomLastLeftOf
            bottomLastLeftOf = element.attributes.getNamedItem("android:id").nodeValue

            bottomTargetDocumentEditor.file.adoptNode(element)
            // Elements do not need to be added in the layout order since a layout constraint is used,
            // but in order is easier to make sense of while debugging.
            bottomTargetElement.insertBefore(element, bottomInsertBeforeNode)
            bottomInsertBeforeNode = element
        }

        sourceDocumentEditor.close()
    }

    override fun close() {
        arrayOf(
            "@id/bottom_end_container",
            "@id/multiview_button",
        ).forEach {
            bottomTargetDocumentEditor.file.childNodes.findElementByAttributeValue(
                "android:id",
                it
            )?.setAttribute("yt:layout_constraintRight_toLeftOf", bottomLastLeftOf)
        }

        bottomTargetDocumentEditor.close()
    }
}
