package app.revanced.patches.angulus

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val angulusPatch = bytecodePatch(
    name = "Hide ads",
    description = "Hides in app ads",
) {
    compatibleWith("com.drinkplusplus.angulus"("5.0.20"))

    execute {
        // Always returns 1 as the daily measurement count
        angulusAdsFingerprint.method.addInstructions(
            0, 
            """
                const/4 v0, 0x1
                return v0
            """,
            )
    }

}
