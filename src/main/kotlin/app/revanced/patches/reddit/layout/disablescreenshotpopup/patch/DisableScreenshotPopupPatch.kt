package app.revanced.patches.reddit.layout.disablescreenshotpopup.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.reddit.layout.disablescreenshotpopup.annotations.DisableScreenshotPopupCompatibility
import app.revanced.patches.reddit.layout.disablescreenshotpopup.fingerprints.DisableScreenshotPopupFingerprint

@Patch
@Name("Disable screenshot popup")
@Description("Disables the popup that shows up when taking a screenshot.")
@DisableScreenshotPopupCompatibility
class DisableScreenshotPopupPatch : BytecodePatch(
    listOf(DisableScreenshotPopupFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        DisableScreenshotPopupFingerprint.result?.mutableMethod?.addInstruction(0, "return-void")
            ?: throw DisableScreenshotPopupFingerprint.exception
    }
}
