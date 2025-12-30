package app.revanced.patches.tumblr.annoyances.popups

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disableGiftMessagePopupPatch = bytecodePatch(
    name = "Disable gift message popup",
    description = "Disables the popup suggesting to buy TumblrMart items for other people.",
) {
    compatibleWith("com.tumblr")

    apply {
        showGiftMessagePopupFingerprint.method.addInstructions(0, "return-void")
    }
}
