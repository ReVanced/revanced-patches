package app.revanced.patches.jakdojade.ad

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    name = "Disable ads",
) {
    compatibleWith("com.citynav.jakdojade.pl.android")

    execute {
        // Spoof isPremium() to always return true.
        // We can do this beacuse Jakdojade Premium's only feature is ad removal.
        isPremiumFingerprint.method.returnEarly(true)

        // Spoof Premium renewal date in the UI.
        // We need to do the spoofing in order to avoid app crashes when opening profile menu.
        getPremiumRenewalDateFingerprint.method.replaceInstructions(
            0,
            """
                const-string v0, ""
                return-object v0
            """
        )
        // Spoof Premium type.
        getGoogleProductFingerprint.method.replaceInstructions(
            0,
            """
                sget-object v0, Lcom/citynav/jakdojade/pl/android/billing/output/GoogleProduct;->PREMIUM_YEARLY_V4:Lcom/citynav/jakdojade/pl/android/billing/output/GoogleProduct;
                return-object v0
            """
        )
    }
}
