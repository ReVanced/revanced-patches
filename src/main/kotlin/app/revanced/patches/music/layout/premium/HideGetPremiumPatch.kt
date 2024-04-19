package app.revanced.patches.music.layout.premium

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.music.layout.premium.fingerprints.AccountMenuFooterFingerprint
import app.revanced.patches.music.layout.premium.fingerprints.HideGetPremiumFingerprint
import app.revanced.patches.music.layout.premium.fingerprints.MembershipSettingsFingerprint
import app.revanced.util.exception
import app.revanced.util.indexOfFirstWideLiteralInstructionValue
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.Reference

@Patch(
    name = "Hide 'Get Music Premium' label",
    description = "Hides the \"Get Music Premium\" label from the account menu and settings.",
    dependencies = [HideGetPremiumResourcePatch::class],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")],
)
@Suppress("unused")
object HideGetPremiumPatch : BytecodePatch(
    setOf(
        AccountMenuFooterFingerprint,
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

        AccountMenuFooterFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = indexOfFirstWideLiteralInstructionValue(HideGetPremiumResourcePatch.privacyTosFooterId) + 4
                targetReference = getInstruction<ReferenceInstruction>(targetIndex + 1).reference

                with(
                    context
                        .toMethodWalker(this)
                        .nextMethod(targetIndex, true)
                        .getMethod() as MutableMethod
                ) {
                    this.implementation!!.instructions.apply {
                        for ((index, instruction) in withIndex()) {
                            if (instruction.opcode != Opcode.IGET_OBJECT) continue

                            if (getInstruction<ReferenceInstruction>(index).reference == targetReference) {
                                val targetRegister =
                                    getInstruction<OneRegisterInstruction>(index + 2).registerA

                                addInstruction(
                                    index,
                                    "const/16 v$targetRegister, 0x8"
                                )

                                break
                            }
                        }
                    }
                }
            }
        } ?: throw AccountMenuFooterFingerprint.exception

        MembershipSettingsFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructions(
                    0, """
                        const/4 v0, 0x0
                        return-object v0
                        """
                )
            }
        } ?: throw MembershipSettingsFingerprint.exception
    }
    
    private lateinit var targetReference: Reference
}
