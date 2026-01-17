package app.revanced.patches.reddit.misc.tracking.url

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused", "ObjectPropertyName")
val `Sanitize sharing links` by creatingBytecodePatch(
    description = "Removes the tracking query parameters from shared links.",
) {
    compatibleWith("com.reddit.frontpage")

    apply {
        shareLinkFormatterFingerprint.method.addInstructions(
            0,
            "return-object p0",
        )
    }
}
