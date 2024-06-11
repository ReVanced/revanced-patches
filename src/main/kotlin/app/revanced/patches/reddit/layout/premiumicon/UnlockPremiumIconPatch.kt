package app.revanced.patches.reddit.layout.premiumicon

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockPremiumIconPatch = bytecodePatch(
    name = "Unlock premium Reddit icons",
    description = "Unlocks the premium Reddit icons.",
) {
    compatibleWith("com.reddit.frontpage")

    val hasPremiumIconAccessResult by hasPremiumIconAccessFingerprint

    execute {
        hasPremiumIconAccessResult.mutableMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}