package app.revanced.patches.youtube.layout.createbutton.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.layout.createbutton.annotations.CreateButtonCompatibility
import app.revanced.patches.youtube.layout.createbutton.fingerprints.CreateButtonFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.mapping.patch.ResourceIdMappingProviderResourcePatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@DependsOn([IntegrationsPatch::class, ResourceIdMappingProviderResourcePatch::class, SettingsPatch::class])
@Name("disable-create-button")
@Description("Hides the create button in the navigation bar.")
@CreateButtonCompatibility
@Version("0.0.1")
class CreateButtonRemoverPatch : BytecodePatch(
    listOf(
        CreateButtonFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_create_button_enabled",
                StringResource("revanced_create_button_enabled_title", "Show create button"),
                false,
                StringResource("revanced_create_button_summary_on", "Create button is shown."),
                StringResource("revanced_create_button_summary_off", "Create button is hidden.")
            )
        )

        val result = CreateButtonFingerprint.result!!

        // Get the required register which holds the view object we need to pass to the method hideCreateButton
        val implementation = result.mutableMethod.implementation!!

        val imageOnlyLayout =
            ResourceIdMappingProviderResourcePatch.resourceMappings.single { it.type == "layout" && it.name == "image_only_tab" }

        val imageOnlyLayoutConstIndex =
            implementation.instructions.indexOfFirst { (it as? WideLiteralInstruction)?.wideLiteral == imageOnlyLayout.id }

        val (instructionIndex, instruction) = implementation.instructions.drop(imageOnlyLayoutConstIndex).withIndex()
            .first {
                (((it.value as? ReferenceInstruction)?.reference) as? MethodReference)?.definingClass?.contains("PivotBar")
                    ?: false
            }

        if (instruction.opcode != Opcode.INVOKE_VIRTUAL) return PatchResultError("Could not find the correct instruction")

        val moveResultIndex = imageOnlyLayoutConstIndex + instructionIndex + 1
        val moveResultInstruction = implementation.instructions[moveResultIndex] as OneRegisterInstruction

        // Hide the button view via proxy by passing it to the hideCreateButton method
        result.mutableMethod.addInstruction(
            moveResultIndex + 1,
            "invoke-static { v${moveResultInstruction.registerA} }, Lapp/revanced/integrations/patches/HideCreateButtonPatch;->hideCreateButton(Landroid/view/View;)V"
        )

        return PatchResultSuccess()
    }
}
