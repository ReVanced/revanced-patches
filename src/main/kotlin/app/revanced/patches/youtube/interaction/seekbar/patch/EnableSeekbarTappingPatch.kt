package app.revanced.patches.youtube.interaction.seekbar.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.interaction.seekbar.annotation.SeekbarTappingCompatibility
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.AccessibilityPlayerProgressTimeFingerprint
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.SeekbarTappingFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.instruction.NarrowLiteralInstruction
import org.jf.dexlib2.iface.instruction.formats.Instruction35c

@Patch
@DependsOn([IntegrationsPatch::class, EnableSeekbarTappingResourcePatch::class])
@Name("seekbar-tapping")
@Description("Enables tap-to-seek on the seekbar of the video player.")
@SeekbarTappingCompatibility
@Version("0.0.1")
class EnableSeekbarTappingPatch : BytecodePatch(
    listOf(AccessibilityPlayerProgressTimeFingerprint, SeekbarTappingFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        // Find the required methods to tap the seekbar.
        val seekbarTappingMethods =
            AccessibilityPlayerProgressTimeFingerprint.result?.classDef?.methods?.let { methods ->
                buildMap {
                    // find the methods which tap the seekbar
                    methods.forEach { method ->
                        if (method.implementation == null) return@forEach

                        val instructions = method.implementation!!.instructions

                        // The method has more than 7 instructions.
                        if (instructions.count() < 7) return@forEach

                        // The 7th instruction has the opcode CONST_4.
                        val instruction = instructions.elementAt(6)
                        if (instruction.opcode != Opcode.CONST_4) return@forEach

                        // the literal for this instruction has to be either 1 or 2.
                        val literal = (instruction as NarrowLiteralInstruction).narrowLiteral

                        // Based on the literal, determine which method is which.
                        if (literal == 1) this["P"] = method
                        if (literal == 2) this["O"] = method
                    }
                }
            }

        seekbarTappingMethods ?: return AccessibilityPlayerProgressTimeFingerprint.toErrorResult()

        SeekbarTappingFingerprint.result?.let {
            val insertIndex = it.scanResult.patternScanResult!!.endIndex - 1

            it.mutableMethod.apply {
                val thisInstanceRegister = getInstruction<Instruction35c>(insertIndex - 1).registerC

                val freeRegister = 0
                val xAxisRegister = 2

                val pMethod = seekbarTappingMethods["P"]!!
                val oMethod = seekbarTappingMethods["O"]!!

                fun Method.toInvokeInstructionString() =
                    "invoke-virtual { v$thisInstanceRegister, v$xAxisRegister }, $definingClass->$name(I)V"

                addInstructionsWithLabels(
                    insertIndex,
                    """
                        invoke-static { }, Lapp/revanced/integrations/patches/SeekbarTappingPatch;->seekbarTappingEnabled()Z
                        move-result v$freeRegister
                        if-eqz v$freeRegister, :disabled
                        ${oMethod.toInvokeInstructionString()}
                        ${pMethod.toInvokeInstructionString()}
                    """,
                    ExternalLabel("disabled", getInstruction(insertIndex))
                )
            }
        } ?: return SeekbarTappingFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}