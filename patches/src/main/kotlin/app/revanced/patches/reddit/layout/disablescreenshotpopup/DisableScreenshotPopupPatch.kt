package app.revanced.patches.reddit.layout.disablescreenshotpopup

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused", "ObjectPropertyName")
val `Disable screenshot popup` by creatingBytecodePatch(
    description = "Disables the popup that shows up when taking a screenshot.",
) {
    compatibleWith("com.reddit.frontpage")

    apply {
        disableScreenshotPopupMethod.addInstruction(0, "return-void")
    }
}
