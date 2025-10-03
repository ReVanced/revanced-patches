package app.revanced.patches.reddit.misc.tracking.url

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val sanitizeUrlQueryPatch = bytecodePatch(
    name = "Sanitize sharing links",
    description = "Removes the tracking query parameters from shared links.",
) {
    compatibleWith("com.reddit.frontpage")

    execute {
        shareLinkFormatterFingerprint.method.addInstructions(
            0,
            "return-object p0",
        )
    }
}
