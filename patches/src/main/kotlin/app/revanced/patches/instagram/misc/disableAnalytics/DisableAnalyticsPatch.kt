package app.revanced.patches.instagram.misc.disableAnalytics

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.instagram.shared.replaceStringWithBogus

@Suppress("unused")
val disableAnalyticsPatch = bytecodePatch(
    name = "Disable analytics",
    description = "Disables analytics that are sent periodically.",
) {
    compatibleWith("com.instagram.android")

    execute {
        // Returns BOGUS as analytics url.
        instagramAnalyticsUrlBuilderMethodFingerprint.method.addInstructions(
        	0,
            """
                const-string v0, "BOGUS"
                return-object v0
            """
        )

        // Replaces analytics url as BOGUS.
        facebookAnalyticsUrlInitMethodFingerprint.replaceStringWithBogus(TARGET_URL)
    }
}

