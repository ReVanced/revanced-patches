package app.revanced.patches.reddit.layout.premiumicon

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockPremiumIconsPatch = bytecodePatch(
    name = "Unlock Premium icons",
    description = "Unlocks the Reddit Premium icons.",
) {
    compatibleWith("com.reddit.frontpage")

    execute {
        hasPremiumIconAccessFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}

@Deprecated("Patch was renamed", ReplaceWith("unlockPremiumIconsPatch"))
@Suppress("unused")
val unlockPremiumIconPatch = bytecodePatch{
    dependsOn(unlockPremiumIconsPatch)
}