package app.revanced.patches.youtube.layout.hide.infocards

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.layout.hide.infocards.fingerprints.InfocardsIncognitoFingerprint
import app.revanced.patches.youtube.layout.hide.infocards.fingerprints.InfocardsIncognitoParentFingerprint
import app.revanced.patches.youtube.layout.hide.infocards.fingerprints.InfocardsMethodCallFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Patch(
    name = "Hide info cards",
    description = "Adds an option to hide info cards that creators add in the video player.",
    dependencies = [
        IntegrationsPatch::class,
        LithoFilterPatch::class,
        HideInfocardsResourcePatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43"
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
    private const val FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/components/HideInfoCardsFilterPatch;"

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
                       " Lapp/revanced/integrations/youtube/patches/HideInfoCardsPatch;->hideInfoCardsIncognito(Landroid/view/View;)V"
           )
        }

        with(InfocardsMethodCallFingerprint.result!!) {
            val hideInfoCardsCallMethod = mutableMethod

            val invokeInterfaceIndex = scanResult.patternScanResult!!.endIndex
            val toggleRegister = hideInfoCardsCallMethod.implementation!!.registerCount - 1

            hideInfoCardsCallMethod.addInstructionsWithLabels(
                invokeInterfaceIndex,
                """
                    invoke-static {}, Lapp/revanced/integrations/youtube/patches/HideInfoCardsPatch;->hideInfoCardsMethodCall()Z
                    move-result v$toggleRegister
                    if-nez v$toggleRegister, :hide_info_cards
                """,
                ExternalLabel(
                    "hide_info_cards", hideInfoCardsCallMethod.getInstruction(invokeInterfaceIndex + 1)
                )
            )
        }

        // Info cards can also appear as litho components.
        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)
    }
}