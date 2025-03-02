package app.revanced.patches.twitch.ad.shared.util

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel

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
    ) = with(classBy { classDefType == it.type }?.mutableClass) {
        this ?: return false

        methods.filter { it.name in methodNames }.forEach {
            val retInstruction = when (returnMethod.returnType) {
                'V' -> "return-void"
                'Z' ->
                    """
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
                ExternalLabel(skipLabelName, it.getInstruction(0)),
            )
        }

        true
    }

    block(::createConditionInstructions, BytecodePatchContext::blockMethods)
}

class ReturnMethod(val returnType: Char, val value: String) {
    companion object {
        val default = ReturnMethod('V', "")
    }
}
