package app.revanced.patches.youtube.layout.autoplaybutton.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.layout.autoplaybutton.annotations.AutoplayButtonCompatibility
import app.revanced.patches.youtube.layout.autoplaybutton.fingerprints.AutoNavInformerFingerprint
import app.revanced.patches.youtube.layout.autoplaybutton.fingerprints.LayoutConstructorFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.mapping.patch.ResourceMappingResourcePatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference
import org.jf.dexlib2.iface.instruction.Instruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class, ResourceMappingResourcePatch::class])
@Name("hide-autoplay-button")
@Description("Hides the autoplay button in the video player.")
@AutoplayButtonCompatibility
@Version("0.0.1")
class HideAutoplayButtonPatch : BytecodePatch(
    listOf(
        LayoutConstructorFingerprint, AutoNavInformerFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_autoplay_button_enabled",
                StringResource("revanced_autoplay_button_enabled_title", "Show autoplay button"),
                false,
                StringResource("revanced_autoplay_button_summary_on", "Autoplay button is shown"),
                StringResource("revanced_autoplay_button_summary_off", "Autoplay button is hidden")
            )
        )

        val autoNavInformerMethod = AutoNavInformerFingerprint.result!!.mutableMethod

        val layoutGenMethodResult = LayoutConstructorFingerprint.result!!
        val layoutGenMethod = layoutGenMethodResult.mutableMethod
        val layoutGenMethodInstructions = layoutGenMethod.implementation!!.instructions

        // resolve the offsets such as ...
        val autoNavPreviewStubId = ResourceMappingResourcePatch.resourceMappings.single {
            it.name == "autonav_preview_stub"
        }.id
        // where to insert the branch instructions and ...
        val insertIndex = layoutGenMethodInstructions.indexOfFirst {
            (it as? WideLiteralInstruction)?.wideLiteral == autoNavPreviewStubId
        }
        // where to branch away
        val branchIndex = layoutGenMethodInstructions.subList(insertIndex + 1, layoutGenMethodInstructions.size - 1).indexOfFirst {
            ((it as? ReferenceInstruction)?.reference as? MethodReference)?.name == "addOnLayoutChangeListener"
        } + 2

        val jumpInstruction = layoutGenMethodInstructions[insertIndex + branchIndex] as Instruction
        layoutGenMethod.addInstructions(
            insertIndex, """
                invoke-static {}, Lapp/revanced/integrations/patches/HideAutoplayButtonPatch;->isButtonShown()Z
                move-result v11
                if-eqz v11, :hidden
            """, listOf(ExternalLabel("hidden", jumpInstruction))
        )

        //force disable autoplay since it's hard to do without the button
        autoNavInformerMethod.addInstructions(
            0, """
            invoke-static {}, Lapp/revanced/integrations/patches/HideAutoplayButtonPatch;->isButtonShown()Z
            move-result v0
            if-nez v0, :hidden
            const/4 v0, 0x0
            return v0
            :hidden
            nop
        """
        )

        return PatchResultSuccess()
    }
}