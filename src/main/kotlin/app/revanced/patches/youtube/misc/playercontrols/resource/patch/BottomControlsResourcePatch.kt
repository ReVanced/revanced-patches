package app.revanced.patches.youtube.misc.playercontrols.resource.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.DomFileEditor
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import app.revanced.patches.youtube.misc.playercontrols.annotation.PlayerControlsCompatibility

@Name("bottom-controls-resource-patch")
@DependsOn([FixLocaleConfigErrorPatch::class])
@Description("Manages the resources for the bottom controls of the YouTube player.")
@PlayerControlsCompatibility
@Version("0.0.1")
class BottomControlsResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        resourceContext = context
        targetXmlEditor = context.xmlEditor[TARGET_RESOURCE]

        return PatchResultSuccess()
    }

    companion object {
        internal const val TARGET_RESOURCE_NAME = "youtube_controls_bottom_ui_container.xml"
        private const val TARGET_RESOURCE = "res/layout/$TARGET_RESOURCE_NAME"

        private lateinit var resourceContext: ResourceContext
        private lateinit var targetXmlEditor: DomFileEditor

        // The element to which to add the new elements to
        private var lastLeftOf = "fullscreen_button"



        /**
         * Add new controls to the bottom of the YouTube player.
         * @param hostYouTubeControlsBottomUiResourceName The hosting resource name containing the elements.
         */
        internal fun addControls(hostYouTubeControlsBottomUiResourceName: String) {
            val sourceXmlEditor =
                resourceContext.xmlEditor[this::class.java.classLoader.getResourceAsStream(
                    hostYouTubeControlsBottomUiResourceName
                )!!]

            val targetElement =
                "android.support.constraint.ConstraintLayout"

            val hostElements = sourceXmlEditor.file.getElementsByTagName(targetElement).item(0).childNodes

            val destinationResourceFile = this.targetXmlEditor.file
            val destinationElement =
                destinationResourceFile.getElementsByTagName(targetElement).item(0)

            for (index in 1 until hostElements.length) {
                val element = hostElements.item(index).cloneNode(true)

                // if the element has no attributes theres no point to adding it to the destination
                if (!element.hasAttributes()) continue

                // set the elements lastLeftOf attribute to the lastLeftOf value
                val namespace = "@+id"
                element.attributes.getNamedItem("yt:layout_constraintRight_toLeftOf").nodeValue =
                    "$namespace/$lastLeftOf"

                // set lastLeftOf attribute to the the current element
                val nameSpaceLength = 4
                lastLeftOf = element.attributes.getNamedItem("android:id").nodeValue.substring(nameSpaceLength)

                // copy the element
                destinationResourceFile.adoptNode(element)
                destinationElement.appendChild(element)
            }
            sourceXmlEditor.close()
        }
    }

    override fun close() {
        targetXmlEditor.close()
    }
}