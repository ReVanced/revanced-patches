package app.revanced.patches.tumblr.annoyances.popups

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.matchOrThrow

@Suppress("unused")
val disableGiftMessagePopupPatch = bytecodePatch(
    name = "Disable gift message popup",
    description = "Disables the popup suggesting to buy TumblrMart items for other people.",
) {
    compatibleWith("com.tumblr")

    execute {
        showGiftMessagePopupFingerprint.matchOrThrow.method.addInstructions(0, "return-void")
    }
}
