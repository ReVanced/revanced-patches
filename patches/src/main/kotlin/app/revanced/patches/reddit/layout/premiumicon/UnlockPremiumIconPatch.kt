package app.revanced.patches.reddit.layout.premiumicon

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Unlock Premium icons` by creatingBytecodePatch(
    description = "Unlocks the Reddit Premium icons.",
) {
    compatibleWith("com.reddit.frontpage")

    apply {
        hasPremiumIconAccessMethod.returnEarly(true)
    }
}
