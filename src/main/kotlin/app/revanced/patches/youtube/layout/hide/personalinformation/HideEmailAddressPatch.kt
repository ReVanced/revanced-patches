package app.revanced.patches.youtube.layout.hide.personalinformation

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.layout.hide.personalinformation.fingerprints.AccountSwitcherAccessibilityLabelFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide email address",
    description = "Hides the email address in the account switcher.",
    dependencies = [IntegrationsPatch::class, HideEmailAddressResourcePatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.16.37",
                "18.19.35",
                "18.20.39",
                "18.23.35",
                "18.29.38",
                "18.32.39"
            ]
        )
    ]
)
@Suppress("unused")
object HideEmailAddressPatch : BytecodePatch(
    setOf(AccountSwitcherAccessibilityLabelFingerprint)
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
