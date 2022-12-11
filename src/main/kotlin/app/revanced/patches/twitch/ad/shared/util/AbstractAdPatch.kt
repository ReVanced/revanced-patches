package app.revanced.patches.twitch.ad.shared.util

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel

abstract class AbstractAdPatch(
    val conditionCall: String,
    val skipLabelName: String,
    internal val fingerprints: Iterable<MethodFingerprint>? = null,
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
                val retIntructions = when(returnMethod.returnType) {
                    'V' -> "return-void"
                    'Z' -> """
                        const/4 v0, ${returnMethod.value}
                        return v0
                    """
                    else -> throw NotImplementedError()
                }
                it.addInstructions(
                    0,
                    """
                        ${createConditionInstructions("v0")}
                        $retIntructions
                    """,
                    listOf(ExternalLabel(skipLabelName, it.instruction(0)))
                )
            }
            true
        }
    }

}