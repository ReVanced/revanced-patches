package app.revanced.patches.tumblr.annoyances.popups.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.tumblr.annoyances.popups.fingerprints.ShowGiftMessagePopupFingerprint

@Patch
@Name("Disable gift message popup")
@Description("Disables the popup suggesting to buy TumblrMart items for other people.")
@Compatibility([Package("com.tumblr")])
class DisableGiftMessagePopupPatch : BytecodePatch(
    listOf(ShowGiftMessagePopupFingerprint)
) {
    override fun execute(context: BytecodeContext) =
        ShowGiftMessagePopupFingerprint.result?.mutableMethod?.addInstructions(0, "return-void")
            ?: throw ShowGiftMessagePopupFingerprint.exception
}