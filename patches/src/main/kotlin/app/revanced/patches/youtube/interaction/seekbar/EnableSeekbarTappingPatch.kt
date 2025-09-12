package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.findFreeRegister
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/SeekbarTappingPatch;"

val enableSeekbarTappingPatch = bytecodePatch(
    description = "Adds an option to enable tap to seek on the seekbar of the video player.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "interaction.seekbar.enableSeekbarTappingPatch")

        PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_seekbar_tapping"),
        )

        // Find the required methods to tap the seekbar.
        val seekbarTappingMethods = onTouchEventHandlerFingerprint.let {
            fun getReference(index: Int) = it.method.getInstruction<ReferenceInstruction>(index)
                .reference as MethodReference

            listOf(
                getReference(it.instructionMatches.first().index),
                getReference(it.instructionMatches.last().index)
            )
        }

        seekbarTappingFingerprint.let {
            val insertIndex = it.instructionMatches.last().index + 1

            it.method.apply {
                val thisInstanceRegister = getInstruction<FiveRegisterInstruction>(
                    insertIndex - 1
                ).registerC

                val xAxisRegister = this.getInstruction<FiveRegisterInstruction>(
                    it.instructionMatches[2].index
                ).registerD

                val freeRegister = findFreeRegister(
                    insertIndex, thisInstanceRegister, xAxisRegister
                )

                val oMethod = seekbarTappingMethods[0]
                val nMethod = seekbarTappingMethods[1]

                addInstructionsWithLabels(
                    insertIndex,
                    """
                        invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->seekbarTappingEnabled()Z
                        move-result v$freeRegister
                        if-eqz v$freeRegister, :disabled
                        invoke-virtual { v$thisInstanceRegister, v$xAxisRegister }, $oMethod
                        invoke-virtual { v$thisInstanceRegister, v$xAxisRegister }, $nMethod
                    """,
                    ExternalLabel("disabled", getInstruction(insertIndex)),
                )
            }
        }
    }
}
