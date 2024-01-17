package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.OnTouchEventHandlerFingerprint
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.SeekbarTappingFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Seekbar tapping",
    description = "Adds an option to enable tap-to-seek on the seekbar of the video player.",
    dependencies = [
        IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.34"
            ]
        )
    ]
)
@Suppress("unused")
object EnableSeekbarTappingPatch : BytecodePatch(
    setOf(
        OnTouchEventHandlerFingerprint,
        SeekbarTappingFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.INTERACTIONS.addPreferences(
            SwitchPreference(
                "revanced_seekbar_tapping",
                StringResource("revanced_seekbar_tapping_title", "Enable seekbar tapping"),
                StringResource("revanced_seekbar_tapping_summary_on", "Seekbar tapping is enabled"),
                StringResource("revanced_seekbar_tapping_summary_off", "Seekbar tapping is disabled")
            )
        )

        // Find the required methods to tap the seekbar.
        val seekbarTappingMethods = OnTouchEventHandlerFingerprint.result?.let {
            val patternScanResult = it.scanResult.patternScanResult!!

            fun getReference(index: Int) = it.mutableMethod.getInstruction<ReferenceInstruction>(index)
                .reference as MethodReference

            buildMap {
                put("N", getReference(patternScanResult.startIndex))
                put("O", getReference(patternScanResult.endIndex))
            }
        }

        seekbarTappingMethods ?: throw OnTouchEventHandlerFingerprint.exception

        SeekbarTappingFingerprint.result?.let {
            val insertIndex = it.scanResult.patternScanResult!!.endIndex - 1

            it.mutableMethod.apply {
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
                        invoke-static { }, Lapp/revanced/integrations/youtube/patches/SeekbarTappingPatch;->seekbarTappingEnabled()Z
                        move-result v$freeRegister
                        if-eqz v$freeRegister, :disabled
                        ${oMethod.toInvokeInstructionString()}
                        ${nMethod.toInvokeInstructionString()}
                    """,
                    ExternalLabel("disabled", getInstruction(insertIndex))
                )
            }
        } ?: throw SeekbarTappingFingerprint.exception
    }
}