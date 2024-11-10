package app.revanced.patches.amazon

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val deepLinkingPatch = bytecodePatch(
    name = "Always allow deep-linking",
    description = "Open Amazon links, even if the app is not set to handle Amazon links.",
) {
    compatibleWith("com.amazon.mShop.android.shopping")

    execute {
        deepLinkingFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
