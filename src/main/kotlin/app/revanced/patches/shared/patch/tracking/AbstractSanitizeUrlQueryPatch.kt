package app.revanced.patches.shared.patch.tracking

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

abstract class AbstractSanitizeUrlQueryPatch(
    private val descriptor: String,
    private val sharedFingerprints: List<MethodFingerprint>,
    private val additionalFingerprints: List<MethodFingerprint>? = null
) : BytecodePatch(
    buildSet {
        addAll(sharedFingerprints)
        additionalFingerprints?.let(::addAll)
    }
) {
    private fun MethodFingerprint.invoke() {
        result?.let {
            it.mutableMethod.apply {
                val targetIndex = it.scanResult.patternScanResult!!.startIndex
                val targetRegister = getInstruction<TwoRegisterInstruction>(targetIndex).registerA

                addInstructions(
                    targetIndex + 2, """
                        invoke-static {v$targetRegister}, $descriptor->stripQueryParameters(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$targetRegister
                        """
                )
            }
        } ?: throw exception
    }

    override fun execute(context: BytecodeContext) {
        for (fingerprint in sharedFingerprints)
            fingerprint.invoke()
    }
}