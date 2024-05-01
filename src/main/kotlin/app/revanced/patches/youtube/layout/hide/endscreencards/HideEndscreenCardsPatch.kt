package app.revanced.patches.youtube.layout.hide.endscreencards

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.layout.hide.endscreencards.fingerprints.*
import app.revanced.patches.youtube.layout.hide.endscreencards.fingerprints.layoutCircleFingerprint
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c

@Suppress("unused")
val hideEndscreenCardsPatch = bytecodePatch(
    name = "Hide endscreen cards",
    description = "Adds an option to hide suggested video cards at the end of videos.",
) {
    dependsOn(
        integrationsPatch,
        hideEndscreenCardsResourcePatch,
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

    val layoutCircleResult by layoutCircleFingerprint
    val layoutIconResult by layoutIconFingerprint
    val layoutVideoResult by layoutVideoFingerprint

    execute {
        listOf(
            layoutCircleResult,
            layoutIconResult,
            layoutVideoResult,
        ).forEach {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.endIndex + 1
                val viewRegister = getInstruction<Instruction21c>(insertIndex - 1).registerA

                addInstruction(
                    insertIndex,
                    "invoke-static { v$viewRegister }, " +
                        "Lapp/revanced/integrations/youtube/patches/HideEndscreenCardsPatch;->" +
                        "hideEndscreen(Landroid/view/View;)V",
                )
            }
        }
    }
}
