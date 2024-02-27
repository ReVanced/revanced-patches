package app.revanced.patches.shared.patch.captions

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.shared.fingerprints.captions.StartVideoInformerFingerprint
import app.revanced.patches.shared.fingerprints.captions.SubtitleTrackFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

abstract class AbstractAutoCaptionsPatch(
    private val classDescriptor: String
) : BytecodePatch(
    setOf(
        StartVideoInformerFingerprint,
        SubtitleTrackFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        StartVideoInformerFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstruction(
                    0,
                    "invoke-static {}, $classDescriptor->prefetchSubtitleTrack()V"
                )
            }
        } ?: throw StartVideoInformerFingerprint.exception

        SubtitleTrackFingerprint.result?.let {
            val targetMethod = context
                .toMethodWalker(it.method)
                .nextMethod(it.scanResult.patternScanResult!!.startIndex + 1, true)
                .getMethod() as MutableMethod

            targetMethod.apply {
                val insertIndex = implementation!!.instructions.indexOfFirst { instruction ->
                    instruction.opcode == Opcode.INVOKE_VIRTUAL
                            && ((instruction as? ReferenceInstruction)?.reference as? MethodReference)?.returnType == "Z"
                } + 2
                val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex, """
                        invoke-static {v$insertRegister}, $classDescriptor->disableAutoCaptions(Z)Z
                        move-result v$insertRegister
                        """
                )
            }
        } ?: throw SubtitleTrackFingerprint.exception

    }
}