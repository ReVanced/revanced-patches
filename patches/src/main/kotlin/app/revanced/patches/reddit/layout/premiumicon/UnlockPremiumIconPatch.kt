package app.revanced.patches.reddit.layout.premiumicon

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val unlockPremiumIconsPatch = bytecodePatch(
    name = "Unlock Premium icons",
    description = "Unlocks the Reddit Premium icons.",
) {
    compatibleWith("com.reddit.frontpage")

    apply {
        hasPremiumIconAccessMethod.returnEarly(true)
    }
}
