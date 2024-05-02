package app.revanced.patches.tumblr.annoyances.popups

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tumblr.annoyances.popups.fingerprints.showGiftMessagePopupFingerprint

@Suppress("unused")
val disableGiftMessagePopupPatch = bytecodePatch(
    name = "Disable gift message popup",
    description = "Disables the popup suggesting to buy TumblrMart items for other people.",
) {
    compatibleWith("com.tumblr"())

    val showGiftMessagePopupResult by showGiftMessagePopupFingerprint

    execute {
        showGiftMessagePopupResult.mutableMethod.addInstructions(0, "return-void")
    }
}
