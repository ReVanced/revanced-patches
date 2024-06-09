package app.revanced.patches.reddit.layout.disablescreenshotpopup

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.layout.disablescreenshotpopup.fingerprints.disableScreenshotPopupFingerprint

@Suppress("unused")
val disableScreenshotPopupPatch = bytecodePatch(
    name = "Disable screenshot popup",
    description = "Disables the popup that shows up when taking a screenshot.",
) {
    compatibleWith("com.reddit.frontpage")

    val disableScreenshotPopupResult by disableScreenshotPopupFingerprint

    execute {
        disableScreenshotPopupResult.mutableMethod.addInstruction(0, "return-void")
    }
}