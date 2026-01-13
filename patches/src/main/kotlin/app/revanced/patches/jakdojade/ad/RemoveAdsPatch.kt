package app.revanced.patches.jakdojade.ad

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
) {
    compatibleWith("com.citynav.jakdojade.pl.android")

    execute {
        // Spoof isPremium() to always return true
        // We can do this beacuse Jakdojade Premium's only feature is ad removal
        isPremiumFingerprint.method.replaceInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        // Spoof Premium renewal date in the UI
        // We need to do the spoofing in order to avoid app crashes when opening profile menu
        premiumRenewalDateFingerprint.method.replaceInstructions(
            0,
            """
                const-string v0, ""
                return-object v0
            """.trimIndent()
        )
        // Spoof Premium type
        getGoogleProductFingerprint.method.replaceInstructions(
            0,
            """
                sget-object v0, Lcom/citynav/jakdojade/pl/android/billing/output/GoogleProduct;->PREMIUM_YEARLY_V4:Lcom/citynav/jakdojade/pl/android/billing/output/GoogleProduct;
                return-object v0
            """.trimIndent()
        )
    }
}
