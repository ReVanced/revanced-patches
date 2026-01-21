package app.revanced.patches.reddit.layout.premiumicon

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused", "ObjectPropertyName")
val `Unlock Premium icons` by creatingBytecodePatch(
    description = "Unlocks the Reddit Premium icons.",
) {
    compatibleWith("com.reddit.frontpage")

    apply {
        hasPremiumIconAccessMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
