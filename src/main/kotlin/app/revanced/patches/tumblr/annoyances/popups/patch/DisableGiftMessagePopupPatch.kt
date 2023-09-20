package app.revanced.patches.tumblr.annoyances.popups.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.tumblr.annoyances.popups.fingerprints.ShowGiftMessagePopupFingerprint

@Patch(
    name = "Disable gift message popup",
    description = "Disables the popup suggesting to buy TumblrMart items for other people.",
    compatiblePackages = [CompatiblePackage("com.tumblr")]
)
@Suppress("unused")
object DisableGiftMessagePopupPatch : BytecodePatch(
    setOf(ShowGiftMessagePopupFingerprint)
) {
    override fun execute(context: BytecodeContext) =
        ShowGiftMessagePopupFingerprint.result?.mutableMethod?.addInstructions(0, "return-void")
            ?: throw ShowGiftMessagePopupFingerprint.exception
}