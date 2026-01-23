package app.revanced.patches.tumblr.annoyances.popups

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Disable gift message popup` by creatingBytecodePatch(
    description = "Disables the popup suggesting to buy TumblrMart items for other people.",
) {
    compatibleWith("com.tumblr")

    apply {
        showGiftMessagePopupMethod.addInstructions(0, "return-void")
    }
}
