package app.revanced.patches.amazon.deeplinking

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.util.exception

@Patch(
    name = "Always allow deep-linking",
    description = "Open Amazon links, even if the app is not set to handle Amazon links.",
    compatiblePackages = [CompatiblePackage("com.amazon.mShop.android.shopping")]
)
@Suppress("unused")
object DeepLinkingPatch : BytecodePatch(
    setOf(DeepLinkingFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        DeepLinkingFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        ) ?: throw DeepLinkingFingerprint.exception
    }
}
