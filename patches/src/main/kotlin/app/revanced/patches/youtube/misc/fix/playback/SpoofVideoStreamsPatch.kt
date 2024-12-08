package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.patcher.patch.bytecodePatch

@Deprecated("Use app.revanced.patches.youtube.misc.spoof.spoofVideoStreamsPatch instead.")
@Suppress("unused")
val spoofVideoStreamsPatch = bytecodePatch {
    dependsOn(app.revanced.patches.youtube.misc.spoof.spoofVideoStreamsPatch)
}
