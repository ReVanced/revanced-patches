package app.revanced.patches.twitch.ad.shared.util

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel

abstract class BaseAdPatch(
    val conditionCall: String,
    val skipLabelName: String,
    internal val fingerprints: Set<MethodFingerprint> = emptySet(),
) : BytecodePatch(fingerprints) {

    protected fun createConditionInstructions(register: String = "v0") = """
        invoke-static { }, $conditionCall
        move-result $register
        if-eqz $register, :$skipLabelName
    """

    protected data class ReturnMethod(val returnType: Char = 'V', val value: String = "")

    protected fun BytecodeContext.blockMethods(clazz: String, vararg methodNames: String, returnMethod: ReturnMethod = ReturnMethod()): Boolean {

        return with(findClass(clazz)?.mutableClass) {
            this ?: return false

            this.methods.filter { methodNames.contains(it.name) }.forEach {
                val retInstruction = when (returnMethod.returnType) {
                    'V' -> "return-void"
                    'Z' -> """
                        const/4 v0, ${returnMethod.value}
                        return v0
                    """
                    else -> throw NotImplementedError()
                }
                it.addInstructionsWithLabels(
                    0,
                    """
                        ${createConditionInstructions("v0")}
                        $retInstruction
                    """,
                    ExternalLabel(skipLabelName, it.getInstruction(0))
                )
            }
            true
        }
    }

}