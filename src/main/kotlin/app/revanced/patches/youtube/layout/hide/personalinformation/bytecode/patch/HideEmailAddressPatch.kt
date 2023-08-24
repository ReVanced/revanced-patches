package app.revanced.patches.youtube.layout.hide.personalinformation.bytecode.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.hide.personalinformation.annotations.HideEmailAddressCompatibility
import app.revanced.patches.youtube.layout.hide.personalinformation.bytecode.fingerprints.AccountSwitcherAccessibilityLabelFingerprint
import app.revanced.patches.youtube.layout.hide.personalinformation.resource.patch.HideEmailAddressResourcePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@DependsOn([IntegrationsPatch::class, HideEmailAddressResourcePatch::class])
@Name("Hide email address")
@Description("Hides the email address in the account switcher.")
@HideEmailAddressCompatibility
class HideEmailAddressPatch : BytecodePatch(
    listOf(
        AccountSwitcherAccessibilityLabelFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        AccountSwitcherAccessibilityLabelFingerprint.result?.let {
            it.mutableMethod.apply {
                val setVisibilityConstIndex = it.scanResult.patternScanResult!!.endIndex

                val setVisibilityConstRegister =
                    getInstruction<OneRegisterInstruction>(setVisibilityConstIndex - 2).registerA

                addInstructions(
                    setVisibilityConstIndex,
                    """
                        invoke-static {v$setVisibilityConstRegister}, Lapp/revanced/integrations/patches/HideEmailAddressPatch;->hideEmailAddress(I)I
                        move-result v$setVisibilityConstRegister
                    """
                )
            }
        } ?: throw AccountSwitcherAccessibilityLabelFingerprint.exception
    }
}
