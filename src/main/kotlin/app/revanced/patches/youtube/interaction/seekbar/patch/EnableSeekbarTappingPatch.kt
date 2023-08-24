package app.revanced.patches.youtube.interaction.seekbar.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.interaction.seekbar.annotation.SeekbarTappingCompatibility
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.OnTouchEventHandlerFingerprint
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.SeekbarTappingFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch
@DependsOn([IntegrationsPatch::class, EnableSeekbarTappingResourcePatch::class])
@Name("Seekbar tapping")
@Description("Enables tap-to-seek on the seekbar of the video player.")
@SeekbarTappingCompatibility
class EnableSeekbarTappingPatch : BytecodePatch(
    listOf(OnTouchEventHandlerFingerprint, SeekbarTappingFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        // Find the required methods to tap the seekbar.
        val seekbarTappingMethods = OnTouchEventHandlerFingerprint.result?.let {
            val patternScanResult = it.scanResult.patternScanResult!!

            fun getReference(index: Int) = it.mutableMethod.getInstruction<ReferenceInstruction>(index)
                .reference as MethodReference

            buildMap {
                put("O", getReference(patternScanResult.endIndex))
                put("N", getReference(patternScanResult.startIndex))
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
                        invoke-static { }, Lapp/revanced/integrations/patches/SeekbarTappingPatch;->seekbarTappingEnabled()Z
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