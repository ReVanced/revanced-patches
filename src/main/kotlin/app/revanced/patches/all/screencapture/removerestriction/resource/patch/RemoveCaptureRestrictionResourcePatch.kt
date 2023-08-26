package app.revanced.patches.all.screencapture.removerestriction.resource.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import org.w3c.dom.Element

@Description("Sets allowAudioPlaybackCapture in manifest to true.")
internal class RemoveCaptureRestrictionResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext) {
        // create an xml editor instance
        context.xmlEditor["AndroidManifest.xml"].use { dom ->
            // get the application node
            val applicationNode = dom
                .file
                .getElementsByTagName("application")
                .item(0) as Element

            // set allowAudioPlaybackCapture attribute to true
            applicationNode.setAttribute("android:allowAudioPlaybackCapture", "true")
        }
    }
}