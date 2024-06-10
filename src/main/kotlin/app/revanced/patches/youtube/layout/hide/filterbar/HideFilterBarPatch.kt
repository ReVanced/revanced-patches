package app.revanced.patches.youtube.layout.hide.filterbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.layout.hide.filterbar.fingerprints.filterBarHeightFingerprint
import app.revanced.patches.youtube.layout.hide.filterbar.fingerprints.relatedChipCloudFingerprint
import app.revanced.patches.youtube.layout.hide.filterbar.fingerprints.searchResultsChipBarFingerprint
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

internal const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/HideFilterBarPatch;"

@Suppress("unused")
val hideFilterBarPatch = bytecodePatch(
    name = "Hide filter bar",
    description = "Adds options to hide the category bar at the top of video feeds.",
) {
    dependsOn(
        integrationsPatch,
        hideFilterBarResourcePatch,
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
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        )
    )

    val filterBarHeightResult by filterBarHeightFingerprint
    val relatedChipCloudResult by relatedChipCloudFingerprint
    val searchResultsChipBarResult by searchResultsChipBarFingerprint

    execute {
        fun <RegisterInstruction : OneRegisterInstruction> MethodFingerprintResult.patch(
            insertIndexOffset: Int = 0,
            hookRegisterOffset: Int = 0,
            instructions: (Int) -> String,
        ) = mutableMethod.apply {
            val endIndex = scanResult.patternScanResult!!.endIndex
            val insertIndex = endIndex + insertIndexOffset
            val register = getInstruction<RegisterInstruction>(endIndex + hookRegisterOffset).registerA

            addInstructions(insertIndex, instructions(register))
        }

        filterBarHeightResult.patch<TwoRegisterInstruction> { register ->
            """
                invoke-static { v$register }, $INTEGRATIONS_CLASS_DESCRIPTOR->hideInFeed(I)I
                move-result v$register
            """
        }

        relatedChipCloudResult.patch<OneRegisterInstruction>(1) { register ->
            "invoke-static { v$register }, " +
                "$INTEGRATIONS_CLASS_DESCRIPTOR->hideInRelatedVideos(Landroid/view/View;)V"
        }

        searchResultsChipBarResult.patch<OneRegisterInstruction>(-1, -2) { register ->
            """
                invoke-static { v$register }, $INTEGRATIONS_CLASS_DESCRIPTOR->hideInSearch(I)I
                move-result v$register
            """
        }
    }
}
