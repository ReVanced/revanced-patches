package app.revanced.patches.all.screencapture.removerestriction

import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Element

@Suppress("unused")
val removeCaptureRestrictionResourcePatch = resourcePatch {
    execute { context ->
        context.document["AndroidManifest.xml"].use { document ->
            val applicationNode = document.getElementsByTagName("application").item(0) as Element

            if (!applicationNode.hasAttribute("android:allowAudioPlaybackCapture")) {
                document.createAttribute("android:allowAudioPlaybackCapture")
                    .apply { value = "true" }.let(applicationNode.attributes::setNamedItem)
            }
        }
    }
}