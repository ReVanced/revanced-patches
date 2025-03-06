package app.revanced.patches.youtube.video.videoqualitymenu

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.video.quality.videoQualityPatch

@Suppress("unused")
@Deprecated("Use 'Video Quality' instead.")
val restoreOldVideoQualityMenuPatch = bytecodePatch {
    dependsOn(videoQualityPatch)
}