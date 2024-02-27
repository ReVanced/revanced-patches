package app.revanced.patches.shared.patch.opus

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.fingerprints.opus.CodecReferenceFingerprint
import app.revanced.patches.shared.fingerprints.opus.CodecSelectorFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.Reference

abstract class AbstractOpusCodecsPatch(
    private val descriptor: String
) : BytecodePatch(
    setOf(
        CodecReferenceFingerprint,
        CodecSelectorFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        CodecReferenceFingerprint.result?.mutableMethod?.let {
            it.implementation!!.instructions.apply {
                var targetIndex = 0
                for ((index, instruction) in withIndex()) {
                    if (instruction.opcode != Opcode.INVOKE_STATIC) continue

                    val targetParameter = it.getInstruction<ReferenceInstruction>(index).reference

                    if (targetParameter.toString().endsWith("Ljava/util/Set;")) {
                        targetReference = targetParameter
                        targetIndex = index
                        break
                    }
                }
                if (targetIndex == 0)
                    throw PatchException("Target method not found!")
            }
        } ?: throw CodecReferenceFingerprint.exception

        CodecSelectorFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = it.scanResult.patternScanResult!!.endIndex
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstructionsWithLabels(
                    targetIndex + 1, """
                        invoke-static {}, $descriptor
                        move-result v7
                        if-eqz v7, :mp4a
                        invoke-static {}, $targetReference
                        move-result-object v$targetRegister
                        """, ExternalLabel("mp4a", getInstruction(targetIndex + 1))
                )
            }
        } ?: throw CodecSelectorFingerprint.exception

    }

    lateinit var targetReference: Reference
}
