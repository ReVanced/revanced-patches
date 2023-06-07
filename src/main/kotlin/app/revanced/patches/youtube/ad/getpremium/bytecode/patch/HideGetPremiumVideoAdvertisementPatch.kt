package app.revanced.patches.youtube.ad.getpremium.bytecode.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.ad.getpremium.annotations.HideGetPremiumCompatibility
import app.revanced.patches.youtube.ad.getpremium.bytecode.fingerprints.GetPremiumViewFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction

@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("hide-get-premium")
@HideGetPremiumCompatibility
@Version("0.0.1")
class HideGetPremiumPatch : BytecodePatch(listOf(GetPremiumViewFingerprint,)) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.ADS.addPreferences(
            SwitchPreference(
                "revanced_hide_get_premium",
                StringResource(
                    "revanced_hide_get_premium_title",
                    "Hide YouTube premium advertisement"
                ),
                StringResource(
                    "revanced_hide_get_premium_summary_on",
                    "YouTube Premium advertisements under video player are hidden"
                ),
                StringResource(
                    "revanced_hide_get_premium_summary_off",
                    "YouTube Premium advertisements under video player are shown"
                )
            )
        )

        GetPremiumViewFingerprint.result?.let {
            it.mutableMethod.apply {
                val startIndex = it.scanResult.patternScanResult!!.startIndex
                val measuredWidthRegister = getInstruction<TwoRegisterInstruction>(startIndex).registerA
                val measuredHeightInstruction = getInstruction<TwoRegisterInstruction>(startIndex + 1)

                val measuredHeightRegister = measuredHeightInstruction.registerA
                val tempRegister = measuredHeightInstruction.registerB

                addInstructionsWithLabels(
                    startIndex + 2,
                    """
                        # Override the internal measurement of the layout with zero values.
                        invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->hideGetPremiumView()Z
                        move-result v$tempRegister
                        if-eqz v$tempRegister, :allow
                        const/4 v$measuredWidthRegister, 0x0
                        const/4 v$measuredHeightRegister, 0x0
                        :allow
                        nop
                        # Layout width/height is then passed to a protected class method.
                    """
                )
            }
        } ?: return GetPremiumViewFingerprint.toErrorResult()

        return PatchResultSuccess()
    }

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/HideGetPremiumPatch;"
    }
}
