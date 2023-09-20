package app.revanced.patches.youtube.layout.hide.infocards

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.layout.hide.infocards.fingerprints.InfocardsIncognitoFingerprint
import app.revanced.patches.youtube.layout.hide.infocards.fingerprints.InfocardsIncognitoParentFingerprint
import app.revanced.patches.youtube.layout.hide.infocards.fingerprints.InfocardsMethodCallFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Patch(
    name = "Hide info cards",
    description = "Hides info cards in videos.",
    dependencies = [
        IntegrationsPatch::class,
        HideInfocardsResourcePatch::class
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
object HideInfoCardsPatch : BytecodePatch(
    setOf(
        InfocardsIncognitoParentFingerprint,
        InfocardsMethodCallFingerprint,
    )
) {
    override fun execute(context: BytecodeContext) {
        InfocardsIncognitoFingerprint.also {
            it.resolve(context, InfocardsIncognitoParentFingerprint.result!!.classDef)
        }.result!!.mutableMethod.apply {
            val invokeInstructionIndex = implementation!!.instructions.indexOfFirst {
                it.opcode.ordinal == Opcode.INVOKE_VIRTUAL.ordinal &&
                        ((it as ReferenceInstruction).reference.toString() == "Landroid/view/View;->setVisibility(I)V")
            }

           addInstruction(
               invokeInstructionIndex,
               "invoke-static {v${getInstruction<FiveRegisterInstruction>(invokeInstructionIndex).registerC}}," +
                       " Lapp/revanced/integrations/patches/HideInfoCardsPatch;->hideInfoCardsIncognito(Landroid/view/View;)V"
           )
        }

        with(InfocardsMethodCallFingerprint.result!!) {
            val hideInfoCardsCallMethod = mutableMethod

            val invokeInterfaceIndex = scanResult.patternScanResult!!.endIndex
            val toggleRegister = hideInfoCardsCallMethod.implementation!!.registerCount - 1

            hideInfoCardsCallMethod.addInstructionsWithLabels(
                invokeInterfaceIndex,
                """
                    invoke-static {}, Lapp/revanced/integrations/patches/HideInfoCardsPatch;->hideInfoCardsMethodCall()Z
                    move-result v$toggleRegister
                    if-nez v$toggleRegister, :hide_info_cards
                """,
                ExternalLabel(
                    "hide_info_cards", hideInfoCardsCallMethod.getInstruction(invokeInterfaceIndex + 1)
                )
            )
        }
    }
}