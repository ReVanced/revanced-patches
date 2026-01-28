package app.revanced.patches.twitch.ad.shared.util

import app.revanced.patcher.extensions.ExternalLabel
import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.firstMutableClassDefOrNull
import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch

fun adPatch(
    conditionCall: String,
    skipLabelName: String,
    block: BytecodePatchBuilder.(
        createConditionInstructions: (register: String) -> String,
        blockMethods: BytecodePatchContext.(
            clazz: String,
            methodNames: Set<String>,
            returnMethod: ReturnMethod,
        ) -> Boolean,
    ) -> Unit,
) = bytecodePatch {
    fun createConditionInstructions(register: String) = """
        invoke-static { }, $conditionCall
        move-result $register
        if-eqz $register, :$skipLabelName
    """

    fun BytecodePatchContext.blockMethods(
        classDefType: String,
        methodNames: Set<String>,
        returnMethod: ReturnMethod,
    ): Boolean {
        val classDef = firstMutableClassDefOrNull(classDefType) ?: return false

        classDef.methods.filter { it.name in methodNames }.forEach { method ->
            val retInstruction = when (returnMethod.returnType) {
                'V' -> "return-void"
                'Z' ->
                    """
                        const/4 v0, ${returnMethod.value}
                        return v0
                    """

                else -> throw NotImplementedError()
            }

            method.addInstructionsWithLabels(
                0,
                """
                        ${createConditionInstructions("v0")}
                        $retInstruction
                    """,
                ExternalLabel(skipLabelName, method.getInstruction(0)),
            )
        }

        return true
    }

    block(::createConditionInstructions, BytecodePatchContext::blockMethods)
}

class ReturnMethod(val returnType: Char, val value: String) {
    companion object {
        val default = ReturnMethod('V', "")
    }
}
