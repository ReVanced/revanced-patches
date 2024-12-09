package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.patcher.patch.bytecodePatch

@Deprecated("Use app.revanced.patches.youtube.misc.spoof.userAgentClientSpoofPatch instead.")
@Suppress("unused")
val userAgentClientSpoofPatch = bytecodePatch {
    dependsOn(app.revanced.patches.youtube.misc.spoof.userAgentClientSpoofPatch)
}
