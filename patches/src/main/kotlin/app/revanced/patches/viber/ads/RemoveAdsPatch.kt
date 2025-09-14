package app.revanced.patches.viber

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    name = "Disable Ads",
    description = "Disables ad cards between chats.",
) {
    compatibleWith("com.viber.voip")

    execute {
        val method = adsFreeFingerprint.method

        // Return 1 (true) indicating ads should be disabled
        method.addInstructions(
            0,
            """
                # Always return enabled
                const/4 v0, 0x1
                return v0
            """
        )
    }
}
