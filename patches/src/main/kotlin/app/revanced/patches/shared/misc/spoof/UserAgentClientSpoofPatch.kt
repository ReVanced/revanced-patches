package app.revanced.patches.shared.misc.spoof

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patches.all.misc.transformation.IMethodCall
import app.revanced.patches.all.misc.transformation.filterMapInstruction35c
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private const val USER_AGENT_STRING_BUILDER_APPEND_METHOD_REFERENCE =
    "Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;"

fun userAgentClientSpoofPatch(originalPackageName: String) = transformInstructionsPatch(
    filterMap = { classDef, _, instruction, instructionIndex ->
        filterMapInstruction35c<MethodCall>(
            "Lapp/revanced/extension",
            classDef,
            instruction,
            instructionIndex,
        )
    },
    transform = transform@{ mutableMethod, entry ->
        val (_, _, instructionIndex) = entry

        // Replace the result of context.getPackageName(), if it is used in a user agent string.
        mutableMethod.apply {
            // After context.getPackageName() the result is moved to a register.
            val targetRegister = (
                getInstruction(instructionIndex + 1)
                    as? OneRegisterInstruction ?: return@transform
                ).registerA

            // IndexOutOfBoundsException is technically possible here,
            // but no such occurrences are present in the app.
            val referee = getInstruction(instructionIndex + 2).getReference<MethodReference>()?.toString()

            // Only replace string builder usage.
            if (referee != USER_AGENT_STRING_BUILDER_APPEND_METHOD_REFERENCE) {
                return@transform
            }

            // Do not change the package name in methods that use resources, or for methods that use GmsCore.
            // Changing these package names will result in playback limitations,
            // particularly Android VR background audio only playback.
            val resourceOrGmsStringInstructionIndex = indexOfFirstInstruction {
                val reference = getReference<StringReference>()
                opcode == Opcode.CONST_STRING &&
                    (reference?.string == "android.resource://" || reference?.string == "gcore_")
            }
            if (resourceOrGmsStringInstructionIndex >= 0) {
                return@transform
            }

            // Overwrite the result of context.getPackageName() with the original package name.
            replaceInstruction(
                instructionIndex + 1,
                "const-string v$targetRegister, \"$originalPackageName\"",
            )
        }
    },
)

@Suppress("unused")
private enum class MethodCall(
    override val definedClassName: String,
    override val methodName: String,
    override val methodParams: Array<String>,
    override val returnType: String,
) : IMethodCall {
    GetPackageName(
        "Landroid/content/Context;",
        "getPackageName",
        emptyArray(),
        "Ljava/lang/String;",
    ),
}
