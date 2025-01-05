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
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

val enableSeekbarTappingPatch = bytecodePatch(
    name = "Seekbar tapping",
    description = "Adds an option to enable tap-to-seek on the seekbar of the video player.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            // 18.38.44 patches but crashes on startup.
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.45.38",
            "19.46.42",
            "19.47.53",
        ),
    )

    execute {
        addResources("youtube", "interaction.seekbar.enableSeekbarTappingPatch")

        PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_seekbar_tapping"),
        )

        // Find the required methods to tap the seekbar.
        val patternMatch = onTouchEventHandlerFingerprint.patternMatch!!

        fun getReference(index: Int) = onTouchEventHandlerFingerprint.method.getInstruction<ReferenceInstruction>(index)
            .reference as MethodReference

        val seekbarTappingMethods = buildMap {
            put("N", getReference(patternMatch.startIndex))
            put("O", getReference(patternMatch.endIndex))
        }

        val insertIndex = seekbarTappingFingerprint.filterMatches.last().index - 1

        seekbarTappingFingerprint.method.apply {
            val thisInstanceRegister = getInstruction<Instruction35c>(insertIndex - 1).registerC

            val freeRegister = 0
            val xAxisRegister = 2

            val oMethod = seekbarTappingMethods["O"]!!
            val nMethod = seekbarTappingMethods["N"]!!

            fun MethodReference.toInvokeInstructionString() =
                "invoke-virtual { v$thisInstanceRegister, v$xAxisRegister }, $this"

            addInstructionsWithLabels(
                insertIndex,
                """
                        invoke-static { }, Lapp/revanced/extension/youtube/patches/SeekbarTappingPatch;->seekbarTappingEnabled()Z
                        move-result v$freeRegister
                        if-eqz v$freeRegister, :disabled
                        ${oMethod.toInvokeInstructionString()}
                        ${nMethod.toInvokeInstructionString()}
                    """,
                ExternalLabel("disabled", getInstruction(insertIndex)),
            )
        }
    }
}
