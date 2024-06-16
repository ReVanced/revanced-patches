package app.revanced.patches.reddit.misc.tracking.url

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val sanitizeUrlQueryPatch = bytecodePatch(
    name = "Sanitize sharing links",
    description = "Removes (tracking) query parameters from the URLs when sharing links.",
) {
    compatibleWith("com.reddit.frontpage")

    val shareLinkFormatterFingerprintResult by shareLinkFormatterFingerprint()

    execute {
        shareLinkFormatterFingerprintResult.mutableMethod.addInstructions(
            0,
            "return-object p0",
        )
    }
}
