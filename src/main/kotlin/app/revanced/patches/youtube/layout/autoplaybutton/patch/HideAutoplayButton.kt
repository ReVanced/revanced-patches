package app.revanced.patches.youtube.layout.autoplaybutton.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.removeInstruction
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.layout.autoplaybutton.annotations.AutoplayButtonCompatibility
import app.revanced.patches.youtube.layout.autoplaybutton.fingerprints.AutonavInformerFingerprint
import app.revanced.patches.youtube.layout.autoplaybutton.fingerprints.LayoutConstructorFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.mapping.patch.ResourceIdMappingProviderResourcePatch
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction
import org.jf.dexlib2.iface.instruction.formats.Instruction35c

@Patch
@Dependencies([ResourceIdMappingProviderResourcePatch::class, IntegrationsPatch::class])
@Name("hide-autoplay-button")
@Description("Hides the autoplay button in the video player.")
@AutoplayButtonCompatibility
@Version("0.0.1")
class HideAutoplayButton : BytecodePatch(
    listOf(
        LayoutConstructorFingerprint, AutonavInformerFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val layoutGenMethod = LayoutConstructorFingerprint.result!!.mutableMethod

        val autonavToggle =
            ResourceIdMappingProviderResourcePatch.resourceMappings.single { it.type == "id" && it.name == "autonav_toggle" }
        val autonavPreviewStub =
            ResourceIdMappingProviderResourcePatch.resourceMappings.single { it.type == "id" && it.name == "autonav_preview_stub" }

        val instructions = layoutGenMethod.implementation!!.instructions

        val autonavToggleConstIndex =
            instructions.indexOfFirst { (it as? WideLiteralInstruction)?.wideLiteral == autonavToggle.id } + 4
        val autonavPreviewStubConstIndex =
            instructions.indexOfFirst { (it as? WideLiteralInstruction)?.wideLiteral == autonavPreviewStub.id } + 4

        injectIfBranch(layoutGenMethod, autonavToggleConstIndex)
        injectIfBranch(layoutGenMethod, autonavPreviewStubConstIndex)

        val autonavInformerMethod = AutonavInformerFingerprint.result!!.mutableMethod

        //force disable autoplay since it's hard to do without the button
        autonavInformerMethod.addInstructions(
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

    private fun injectIfBranch(method: MutableMethod, index: Int) {
        val instructions = method.implementation!!.instructions
        val insn = (instructions.get(index) as? Instruction35c)!!
        val methodToCall = insn.reference.toString()

        //remove the invoke-virtual because we want to put it in an if-statement
        method.removeInstruction(index)
        method.addInstructions(
            index, """
            invoke-static {}, Lapp/revanced/integrations/patches/HideAutoplayButtonPatch;->isButtonShown()Z
            move-result v11
            if-eqz v11, :hidebutton
            invoke-virtual {v${insn.registerC}, v${insn.registerD}, v${insn.registerE}}, $methodToCall
            :hidebutton
            nop
        """
        )
    }
}
