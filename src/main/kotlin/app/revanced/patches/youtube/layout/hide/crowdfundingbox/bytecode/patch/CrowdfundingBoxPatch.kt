package app.revanced.patches.youtube.layout.hide.crowdfundingbox.bytecode.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.hide.crowdfundingbox.annotations.CrowdfundingBoxCompatibility
import app.revanced.patches.youtube.layout.hide.crowdfundingbox.bytecode.fingerprints.CrowdfundingBoxFingerprint
import app.revanced.patches.youtube.layout.hide.crowdfundingbox.resource.patch.CrowdfundingBoxResourcePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch
@DependsOn([IntegrationsPatch::class, CrowdfundingBoxResourcePatch::class])
@Name("Hide crowdfunding box")
@Description("Hides the crowdfunding box between the player and video description.")
@CrowdfundingBoxCompatibility
class CrowdfundingBoxPatch : BytecodePatch(
    listOf(
        CrowdfundingBoxFingerprint,
    )
) {
    override fun execute(context: BytecodeContext) {
        CrowdfundingBoxFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.endIndex
                val objectRegister = getInstruction<TwoRegisterInstruction>(insertIndex).registerA

                addInstruction(insertIndex, "invoke-static {v$objectRegister}, $INTEGRATIONS_METHOD_DESCRIPTOR")
            }
        } ?: throw CrowdfundingBoxFingerprint.exception
    }

    private companion object {
        const val INTEGRATIONS_METHOD_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/HideCrowdfundingBoxPatch;->hideCrowdfundingBox(Landroid/view/View;)V"
    }
}
