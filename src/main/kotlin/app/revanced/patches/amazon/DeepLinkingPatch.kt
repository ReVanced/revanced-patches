import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.amazon.deepLinkingFingerprint

@Suppress("unused")
val deepLinkingPatch = bytecodePatch(
    name = "Always allow deep-linking",
    description = "Open Amazon links, even if the app is not set to handle Amazon links.",
) {
    compatibleWith("com.amazon.mShop.android.shopping")

    val deepLinkingFingerprintResult by deepLinkingFingerprint

    execute {
        deepLinkingFingerprintResult.mutableMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
