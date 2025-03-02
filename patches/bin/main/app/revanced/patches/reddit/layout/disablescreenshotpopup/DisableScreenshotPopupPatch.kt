package app.revanced.patches.reddit.layout.disablescreenshotpopup

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disableScreenshotPopupPatch = bytecodePatch(
    name = "Disable screenshot popup",
    description = "Disables the popup that shows up when taking a screenshot.",
) {
    compatibleWith("com.reddit.frontpage")

    execute {
        disableScreenshotPopupFingerprint.method.addInstruction(0, "return-void")
    }
}
