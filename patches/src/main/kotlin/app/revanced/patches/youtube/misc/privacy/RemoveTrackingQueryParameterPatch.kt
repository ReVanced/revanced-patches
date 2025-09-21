package app.revanced.patches.youtube.misc.privacy

import app.revanced.patcher.patch.bytecodePatch

@Deprecated("Patch was renamed", ReplaceWith("sanitizeSharingLinksPatch"))
@Suppress("unused")
val removeTrackingQueryParameterPatch = bytecodePatch{
    dependsOn(sanitizeSharingLinksPatch)
}
