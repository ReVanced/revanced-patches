package app.revanced.patches.reddit.layout.screenshotpopup

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.reddit.layout.screenshotpopup.fingerprints.ScreenshotTakenBannerFingerprint
import app.revanced.patches.reddit.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.reddit.utils.settings.SettingsBytecodePatch.updateSettingsStatus
import app.revanced.patches.reddit.utils.settings.SettingsPatch
import app.revanced.util.exception

@Patch(
    name = "Disable screenshot popup",
    description = "Adds an option to disable the popup that shows up when taking a screenshot.",
    dependencies = [SettingsPatch::class, SharedResourceIdPatch::class],
    compatiblePackages = [CompatiblePackage("com.reddit.frontpage")]
)
@Suppress("unused")
object ScreenshotPopupPatch : BytecodePatch(
    setOf(ScreenshotTakenBannerFingerprint)
) {
    private const val INTEGRATIONS_METHOD_DESCRIPTOR =
        "Lapp/revanced/integrations/reddit/patches/ScreenshotPopupPatch;" +
                "->disableScreenshotPopup()Z"

    override fun execute(context: BytecodeContext) {

        ScreenshotTakenBannerFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructionsWithLabels(
                    0, """
                        invoke-static {}, $INTEGRATIONS_METHOD_DESCRIPTOR
                        move-result v0
                        if-eqz v0, :dismiss
                        return-void
                        """, ExternalLabel("dismiss", getInstruction(0))
                )
            }
        } ?: throw ScreenshotTakenBannerFingerprint.exception

        updateSettingsStatus("ScreenshotPopup")

    }
}
