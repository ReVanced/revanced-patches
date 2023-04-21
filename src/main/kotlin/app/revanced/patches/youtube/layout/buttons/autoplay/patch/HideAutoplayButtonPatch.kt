package app.revanced.patches.youtube.layout.buttons.autoplay.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.buttons.autoplay.annotations.AutoplayButtonCompatibility
import app.revanced.patches.youtube.layout.buttons.autoplay.fingerprints.LayoutConstructorFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import org.jf.dexlib2.iface.instruction.Instruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class, ResourceMappingPatch::class])
@Name("hide-autoplay-button")
@Description("Hides the autoplay button in the video player.")
@AutoplayButtonCompatibility
@Version("0.0.1")
class HideAutoplayButtonPatch : BytecodePatch(
    listOf(LayoutConstructorFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_autoplay_button",
                StringResource("revanced_hide_autoplay_button_title", "Hide autoplay button"),
                true,
                StringResource("revanced_hide_autoplay_button_summary_on", "Autoplay button is hidden"),
                StringResource("revanced_hide_autoplay_button_summary_off", "Autoplay button is shown")
            ),
        )

        LayoutConstructorFingerprint.result?.mutableMethod?.apply {
            val layoutGenMethodInstructions = implementation!!.instructions

            // resolve the offsets such as ...
            val autoNavPreviewStubId = ResourceMappingPatch.resourceMappings.single {
                it.name == "autonav_preview_stub"
            }.id

            // where to insert the branch instructions and ...
            val insertIndex = layoutGenMethodInstructions.indexOfFirst {
                (it as? WideLiteralInstruction)?.wideLiteral == autoNavPreviewStubId
            }

            // where to branch away
            val branchIndex =
                layoutGenMethodInstructions.subList(insertIndex + 1, layoutGenMethodInstructions.size - 1)
                    .indexOfFirst {
                        ((it as? ReferenceInstruction)?.reference as? MethodReference)?.name == "addOnLayoutChangeListener"
                    } + 2

            val jumpInstruction = layoutGenMethodInstructions[insertIndex + branchIndex] as Instruction

            // can be clobbered because this register is overwritten after the injected code
            val clobberRegister = (instruction(insertIndex) as OneRegisterInstruction).registerA

            addInstructions(
                insertIndex,
                """
                invoke-static {}, Lapp/revanced/integrations/patches/HideAutoplayButtonPatch;->isButtonShown()Z
                move-result v$clobberRegister
                if-eqz v$clobberRegister, :hidden
            """, listOf(ExternalLabel("hidden", jumpInstruction))
            )
        } ?: return LayoutConstructorFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}