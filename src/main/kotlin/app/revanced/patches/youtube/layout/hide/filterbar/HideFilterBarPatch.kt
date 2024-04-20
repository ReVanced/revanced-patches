package app.revanced.patches.youtube.layout.hide.filterbar

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.layout.hide.filterbar.fingerprints.FilterBarHeightFingerprint
import app.revanced.patches.youtube.layout.hide.filterbar.fingerprints.RelatedChipCloudFingerprint
import app.revanced.patches.youtube.layout.hide.filterbar.fingerprints.SearchResultsChipBarFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Hide filter bar",
    description = "Adds options to hide the category bar at the top of video feeds.",
    dependencies = [HideFilterBarResourcePatch::class],
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
object HideFilterBarPatch : BytecodePatch(
    setOf(
        RelatedChipCloudFingerprint,
        SearchResultsChipBarFingerprint,
        FilterBarHeightFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/HideFilterBarPatch;"

    override fun execute(context: BytecodeContext) {
        FilterBarHeightFingerprint.patch<TwoRegisterInstruction> { register ->
            """
                invoke-static { v$register }, $INTEGRATIONS_CLASS_DESCRIPTOR->hideInFeed(I)I
                move-result v$register
            """
        }

        RelatedChipCloudFingerprint.patch<OneRegisterInstruction>(1) { register ->
            "invoke-static { v$register }, " +
                    "$INTEGRATIONS_CLASS_DESCRIPTOR->hideInRelatedVideos(Landroid/view/View;)V"
        }

        SearchResultsChipBarFingerprint.patch<OneRegisterInstruction>(-1, -2) { register ->
            """
                invoke-static { v$register }, $INTEGRATIONS_CLASS_DESCRIPTOR->hideInSearch(I)I
                move-result v$register
            """
        }
    }

    /**
     * Patch a [MethodFingerprint] with a given [instructions].
     *
     * @param RegisterInstruction The type of instruction to get the register from.
     * @param insertIndexOffset The offset to add to the end index of the [MethodFingerprint].
     * @param hookRegisterOffset The offset to add to the register of the hook.
     * @param instructions The instructions to add with the register as a parameter.
     */
    private fun <RegisterInstruction : OneRegisterInstruction> MethodFingerprint.patch(
        insertIndexOffset: Int = 0,
        hookRegisterOffset: Int = 0,
        instructions: (Int) -> String
    ) =
        result?.let {
            it.mutableMethod.apply {
                val endIndex = it.scanResult.patternScanResult!!.endIndex

                val insertIndex = endIndex + insertIndexOffset
                val register =
                    getInstruction<RegisterInstruction>(endIndex + hookRegisterOffset).registerA

                addInstructions(insertIndex, instructions(register))
            }
        } ?: throw exception
}
