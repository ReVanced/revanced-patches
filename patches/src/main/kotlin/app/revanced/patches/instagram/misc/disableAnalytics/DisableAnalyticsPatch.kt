package app.revanced.patches.instagram.misc.disableAnalytics

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disableAnalyticsPatch = bytecodePatch(
    name = "Disable analytics",
    description = "Disables analytics that are sent periodically.",
) {
    compatibleWith("com.instagram.android")

    apply {
        // Returns BOGUS as analytics url.
        instagramAnalyticsUrlBuilderMethod.addInstructions(
        	0,
            """
                const-string v0, "BOGUS"
                return-object v0
            """
        )

        // Replaces analytics url as BOGUS.
        facebookAnalyticsUrlInitMethod.addInstructions(
            0,
            """
                const-string v0, "BOGUS"
                return-object v0
            """
        )
    }
}

