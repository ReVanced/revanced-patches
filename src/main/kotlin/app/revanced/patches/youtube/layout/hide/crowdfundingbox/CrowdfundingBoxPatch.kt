package app.revanced.patches.youtube.layout.hide.crowdfundingbox

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.layout.hide.crowdfundingbox.fingerprints.CrowdfundingBoxFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Hide crowdfunding box",
    description = "Hides the crowdfunding box between the player and video description.",
    dependencies = [
        IntegrationsPatch::class,
        CrowdfundingBoxResourcePatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
object CrowdfundingBoxPatch : BytecodePatch(
    setOf(CrowdfundingBoxFingerprint)
) {
    private const val INTEGRATIONS_METHOD_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/HideCrowdfundingBoxPatch;->hideCrowdfundingBox(Landroid/view/View;)V"

    override fun execute(context: BytecodeContext) {
        CrowdfundingBoxFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.endIndex
                val objectRegister = getInstruction<TwoRegisterInstruction>(insertIndex).registerA

                addInstruction(insertIndex, "invoke-static {v$objectRegister}, $INTEGRATIONS_METHOD_DESCRIPTOR")
            }
        } ?: throw CrowdfundingBoxFingerprint.exception
    }
}
