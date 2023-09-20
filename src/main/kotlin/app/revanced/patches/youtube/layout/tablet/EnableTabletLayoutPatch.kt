package app.revanced.patches.youtube.layout.tablet

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.tablet.fingerprints.GetFormFactorFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Enable tablet layout",
    description = "Spoofs the device form factor to a tablet which enables the tablet layout.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [CompatiblePackage("com.google.android.youtube")]
)
@Suppress("unused")
object EnableTabletLayoutPatch : BytecodePatch(
    setOf(GetFormFactorFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_tablet_layout",
                StringResource("revanced_tablet_layout_enabled_title", "Enable tablet layout"),
                StringResource("revanced_tablet_layout_summary_on", "Tablet layout is enabled"),
                StringResource("revanced_tablet_layout_summary_off", "Tablet layout is disabled"),
                StringResource("revanced_tablet_layout_user_dialog_message", "Community posts do not show up on tablet layouts")
            )
        )

        GetFormFactorFingerprint.result?.let {
            it.mutableMethod.apply {
                val returnLargeFormFactorIndex = it.scanResult.patternScanResult!!.endIndex - 4

                addInstructionsWithLabels(
                    0,
                    """
                          invoke-static {}, Lapp/revanced/integrations/patches/EnableTabletLayoutPatch;->enableTabletLayout()Z
                          move-result v0
                          if-nez v0, :is_large_form_factor
                    """,
                    ExternalLabel(
                        "is_large_form_factor",
                        getInstruction(returnLargeFormFactorIndex)
                    )
                )
            }
        } ?: GetFormFactorFingerprint.exception
    }
}
