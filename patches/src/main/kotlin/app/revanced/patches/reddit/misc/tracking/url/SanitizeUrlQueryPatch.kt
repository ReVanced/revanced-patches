package app.revanced.patches.reddit.misc.tracking.url

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val sanitizeSharingLinksPatch = bytecodePatch(
    name = "Sanitize sharing links",
    description = "Removes the tracking query parameters from shared links.",
) {
    compatibleWith("com.reddit.frontpage")

    apply {
        shareLinkFormatterMethod.addInstructions(0, "return-object p0")
    }
}
