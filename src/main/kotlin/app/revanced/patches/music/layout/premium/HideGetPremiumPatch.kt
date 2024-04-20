package app.revanced.patches.music.layout.premium

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.layout.premium.fingerprints.HideGetPremiumFingerprint
import app.revanced.patches.music.layout.premium.fingerprints.MembershipSettingsFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Patch(
    name = "Hide 'Get Music Premium' label",
    description = "Hides the \"Get Music Premium\" label from the account menu and settings.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")],
)
@Suppress("unused")
object HideGetPremiumPatch : BytecodePatch(
    setOf(
        HideGetPremiumFingerprint,
        MembershipSettingsFingerprint,
    ),
) {
    override fun execute(context: BytecodeContext) {
        HideGetPremiumFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.endIndex

                val setVisibilityInstruction = getInstruction<FiveRegisterInstruction>(insertIndex)
                val getPremiumViewRegister = setVisibilityInstruction.registerC
                val visibilityRegister = setVisibilityInstruction.registerD

                replaceInstruction(
                    insertIndex,
                    "const/16 v$visibilityRegister, 0x8",
                )

                addInstruction(
                    insertIndex + 1,
                    "invoke-virtual {v$getPremiumViewRegister, v$visibilityRegister}, " +
                        "Landroid/view/View;->setVisibility(I)V",
                )
            }
        } ?: throw HideGetPremiumFingerprint.exception

        MembershipSettingsFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return-object v0
            """,
        ) ?: throw MembershipSettingsFingerprint.exception
    }
}
