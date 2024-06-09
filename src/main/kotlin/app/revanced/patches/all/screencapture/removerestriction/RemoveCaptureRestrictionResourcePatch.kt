package app.revanced.patches.all.screencapture.removerestriction

import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Element

@Suppress("unused")
internal val removeCaptureRestrictionResourcePatch = resourcePatch(
    description = "Sets allowAudioPlaybackCapture in manifest to true.",
) {
    execute { context ->
        context.document["AndroidManifest.xml"].use { document ->
            // Get the application node.
            val applicationNode =
                document
                    .getElementsByTagName("application")
                    .item(0) as Element

            // Set allowAudioPlaybackCapture attribute to true.
            applicationNode.setAttribute("android:allowAudioPlaybackCapture", "true")
        }
    }
}
