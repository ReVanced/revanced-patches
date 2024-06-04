package app.revanced.patches.youtube.layout.hide.infocards

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.layout.hide.infocards.fingerprints.*
import app.revanced.patches.youtube.layout.hide.infocards.fingerprints.infocardsIncognitoParentFingerprint
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Suppress("unused")
val hideInfoCardsPatch = bytecodePatch(
    name = "Hide info cards",
    description = "Adds an option to hide info cards that creators add in the video player.",
) {
    dependsOn(
        integrationsPatch,
        lithoFilterPatch,
        hideInfocardsResourcePatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
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
            "19.11.43",
        ),
    )

    val infocardsIncognitoParentResult by infocardsIncognitoParentFingerprint
    val infocardsMethodCallResult by infocardsMethodCallFingerprint

    execute { context ->
        infocardsIncognitoFingerprint.apply {
            resolve(context, infocardsIncognitoParentResult.classDef)
        }.result!!.mutableMethod.apply {
            val invokeInstructionIndex = implementation!!.instructions.indexOfFirst {
                it.opcode.ordinal == Opcode.INVOKE_VIRTUAL.ordinal &&
                    ((it as ReferenceInstruction).reference.toString() == "Landroid/view/View;->setVisibility(I)V")
            }

            addInstruction(
                invokeInstructionIndex,
                "invoke-static {v${getInstruction<FiveRegisterInstruction>(invokeInstructionIndex).registerC}}," +
                    " Lapp/revanced/integrations/youtube/patches/HideInfoCardsPatch;->hideInfoCardsIncognito(Landroid/view/View;)V",
            )
        }

        val hideInfoCardsCallMethod = infocardsMethodCallResult.mutableMethod

        val invokeInterfaceIndex = infocardsMethodCallResult.scanResult.patternScanResult!!.endIndex
        val toggleRegister = infocardsMethodCallResult.mutableMethod.implementation!!.registerCount - 1

        hideInfoCardsCallMethod.addInstructionsWithLabels(
            invokeInterfaceIndex,
            """
                    invoke-static {}, Lapp/revanced/integrations/youtube/patches/HideInfoCardsPatch;->hideInfoCardsMethodCall()Z
                    move-result v$toggleRegister
                    if-nez v$toggleRegister, :hide_info_cards
                """,
            ExternalLabel(
                "hide_info_cards",
                hideInfoCardsCallMethod.getInstruction(invokeInterfaceIndex + 1),
            ),
        )

        // Info cards can also appear as Litho components.
        val filterClassDescriptor = "Lapp/revanced/integrations/youtube/patches/components/HideInfoCardsFilterPatch;"
        addLithoFilter(filterClassDescriptor)
    }
}
