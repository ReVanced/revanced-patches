package app.revanced.patches.amazon

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val `Always allow deep-linking` by creatingBytecodePatch(
    description = "Open Amazon links, even if the app is not set to handle Amazon links.",
) {
    compatibleWith("com.amazon.mShop.android.shopping")

    apply {
        deepLinkingMethod.returnEarly(true)
    }
}
