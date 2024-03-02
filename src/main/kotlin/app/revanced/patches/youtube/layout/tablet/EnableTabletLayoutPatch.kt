package app.revanced.patches.youtube.layout.tablet

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.tablet.fingerprints.GetFormFactorFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception

@Patch(
    name = "Enable tablet layout",
    description = "Adds an option to spoof the device form factor to a tablet which enables the tablet layout.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
    compatiblePackages = [CompatiblePackage("com.google.android.youtube")]
)
@Suppress("unused")
object EnableTabletLayoutPatch : BytecodePatch(
    setOf(GetFormFactorFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_tablet_layout")
        )

        GetFormFactorFingerprint.result?.let {
            it.mutableMethod.apply {
                val returnIsLargeFormFactorIndex = getInstructions().lastIndex - 4
                val returnIsLargeFormFactorLabel = getInstruction(returnIsLargeFormFactorIndex)

                addInstructionsWithLabels(
                    0,
                    """
                          invoke-static { }, Lapp/revanced/integrations/youtube/patches/EnableTabletLayoutPatch;->enableTabletLayout()Z
                          move-result v0 # Free register
                          if-nez v0, :is_large_form_factor
                    """,
                    ExternalLabel(
                        "is_large_form_factor",
                        returnIsLargeFormFactorLabel
                    )
                )
            }
        } ?: GetFormFactorFingerprint.exception
    }
}
