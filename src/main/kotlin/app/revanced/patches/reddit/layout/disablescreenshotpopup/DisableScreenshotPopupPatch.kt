package app.revanced.patches.reddit.layout.disablescreenshotpopup

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.layout.disablescreenshotpopup.fingerprints.DisableScreenshotPopupFingerprint

@Patch(
    name = "Disable screenshot popup",
    description = "Disables the popup that shows up when taking a screenshot.",
    compatiblePackages = [CompatiblePackage("com.reddit.frontpage")]
)
@Suppress("unused")
object DisableScreenshotPopupPatch : BytecodePatch(setOf(DisableScreenshotPopupFingerprint)) {
    override fun execute(context: BytecodeContext) {
        DisableScreenshotPopupFingerprint.result?.mutableMethod?.addInstruction(0, "return-void")
            ?: throw DisableScreenshotPopupFingerprint.exception
    }
}
