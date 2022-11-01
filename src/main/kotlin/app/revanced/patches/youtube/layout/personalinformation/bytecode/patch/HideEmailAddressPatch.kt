package app.revanced.patches.youtube.layout.personalinformation.bytecode.patch

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
import app.revanced.patches.youtube.layout.personalinformation.annotations.HideEmailAddressCompatibility
import app.revanced.patches.youtube.layout.personalinformation.bytecode.fingerprints.AccountSwitcherAccessibilityLabelFingerprint
import app.revanced.patches.youtube.layout.personalinformation.resource.patch.HideEmailAddressResourcePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@DependsOn([IntegrationsPatch::class, HideEmailAddressResourcePatch::class])
@Name("hide-email-address")
@Description("Hides the email address in the account switcher.")
@HideEmailAddressCompatibility
@Version("0.0.1")
class HideEmailAddressPatch : BytecodePatch(
    listOf(
        AccountSwitcherAccessibilityLabelFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val accountSwitcherAccessibilityLabelResult = AccountSwitcherAccessibilityLabelFingerprint.result!!
        val accountSwitcherAccessibilityLabelMethod = accountSwitcherAccessibilityLabelResult.mutableMethod

        val setVisibilityConstIndex =
            accountSwitcherAccessibilityLabelResult.scanResult.patternScanResult!!.endIndex

        val setVisibilityConstRegister = (
                accountSwitcherAccessibilityLabelMethod.instruction
                (setVisibilityConstIndex - 2) as OneRegisterInstruction
            ).registerA

        accountSwitcherAccessibilityLabelMethod.addInstructions(
            setVisibilityConstIndex, """
            invoke-static {v$setVisibilityConstRegister}, Lapp/revanced/integrations/patches/HideEmailAddressPatch;->hideEmailAddress(I)I
            move-result v$setVisibilityConstRegister
        """
        )

        return PatchResultSuccess()
    }
}
