package app.revanced.patches.all.screencapture.removerestriction

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import org.w3c.dom.Element

@Patch(description = "Sets allowAudioPlaybackCapture in manifest to true.")
internal object RemoveCaptureRestrictionResourcePatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        context.xmlEditor["AndroidManifest.xml"].use { editor ->
            val document = editor.file

            // get the application node
            val applicationNode =
                document
                    .getElementsByTagName("application")
                    .item(0) as Element

            // set allowAudioPlaybackCapture attribute to true
            applicationNode.setAttribute("android:allowAudioPlaybackCapture", "true")
        }
    }
}
