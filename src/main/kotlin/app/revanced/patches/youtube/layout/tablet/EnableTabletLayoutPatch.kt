package app.revanced.patches.youtube.layout.tablet

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.tablet.fingerprints.GetFormFactorFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10x

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
                val returnCurrentFormFactorIndex = getInstructions().lastIndex - 2

                val returnIsLargeFormFactorLabel = getInstruction(returnCurrentFormFactorIndex - 2)
                val returnFormFactorIndex = getInstruction(returnCurrentFormFactorIndex)

                val insertIndex = returnCurrentFormFactorIndex + 1

                // Replace the labeled instruction with a nop and add the preserved instructions back
                replaceInstruction(returnCurrentFormFactorIndex, BuilderInstruction10x(Opcode.NOP))
                addInstruction(insertIndex, returnFormFactorIndex)

                // Because the labeled instruction is now a nop, we can add our own instructions right after it
                addInstructionsWithLabels(
                    insertIndex,
                    """
                          invoke-static { }, Lapp/revanced/integrations/patches/EnableTabletLayoutPatch;->enableTabletLayout()Z
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
