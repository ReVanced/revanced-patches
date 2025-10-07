package app.revanced.patches.instagram.misc.privacy

import app.revanced.patcher.patch.bytecodePatch

@Deprecated(
    "Patch was moved to a different package",
    ReplaceWith("app.revanced.patches.instagram.misc.share.privacy.sanitizeSharingLinksPatch")
)
@Suppress("unused")
val sanitizeSharingLinksPatch = bytecodePatch {
    dependsOn(app.revanced.patches.instagram.misc.share.privacy.sanitizeSharingLinksPatch)
}
