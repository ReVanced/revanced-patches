package app.revanced.patches.youtube.layout.buttons.autoplay.patch

import app.revanced.extensions.findIndexForIdResource
import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.buttons.autoplay.annotations.AutoplayButtonCompatibility
import app.revanced.patches.shared.fingerprints.LayoutConstructorFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class, ResourceMappingPatch::class])
@Name("Hide autoplay button")
@Description("Hides the autoplay button in the video player.")
@AutoplayButtonCompatibility
class HideAutoplayButtonPatch : BytecodePatch(
    listOf(LayoutConstructorFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_autoplay_button",
                StringResource("revanced_hide_autoplay_button_title", "Hide autoplay button"),
                StringResource("revanced_hide_autoplay_button_summary_on", "Autoplay button is hidden"),
                StringResource("revanced_hide_autoplay_button_summary_off", "Autoplay button is shown")
            ),
        )

        LayoutConstructorFingerprint.result?.mutableMethod?.apply {
            val layoutGenMethodInstructions = implementation!!.instructions

            // resolve the offsets of where to insert the branch instructions and ...
            val insertIndex = findIndexForIdResource("autonav_preview_stub")

            // where to branch away
            val branchIndex =
                layoutGenMethodInstructions.subList(insertIndex + 1, layoutGenMethodInstructions.size - 1)
                    .indexOfFirst {
                        ((it as? ReferenceInstruction)?.reference as? MethodReference)?.name == "addOnLayoutChangeListener"
                    } + 2

            val jumpInstruction = layoutGenMethodInstructions[insertIndex + branchIndex] as Instruction

            // can be clobbered because this register is overwritten after the injected code
            val clobberRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

            addInstructionsWithLabels(
                insertIndex,
                """
                    invoke-static {}, Lapp/revanced/integrations/patches/HideAutoplayButtonPatch;->isButtonShown()Z
                    move-result v$clobberRegister
                    if-eqz v$clobberRegister, :hidden
                """,
                ExternalLabel("hidden", jumpInstruction)
            )
        } ?: throw LayoutConstructorFingerprint.exception
    }
}