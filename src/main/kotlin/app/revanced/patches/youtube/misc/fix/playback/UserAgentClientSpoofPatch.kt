package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patches.all.misc.transformation.IMethodCall
import app.revanced.patches.all.misc.transformation.filterMapInstruction35c
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val ORIGINAL_PACKAGE_NAME = "com.google.android.youtube"
private const val USER_AGENT_STRING_BUILDER_APPEND_METHOD_REFERENCE =
    "Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;"

val userAgentClientSpoofPatch = transformInstructionsPatch(
    filterMap = { classDef, _, instruction, instructionIndex ->
        filterMapInstruction35c<MethodCall>(
            "Lapp/revanced/integrations",
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

            // This can technically also match non-user agent string builder append methods,
            // but no such occurrences are present in the app.
            if (referee != USER_AGENT_STRING_BUILDER_APPEND_METHOD_REFERENCE) {
                return@transform
            }

            // Overwrite the result of context.getPackageName() with the original package name.
            replaceInstruction(
                instructionIndex + 1,
                "const-string v$targetRegister, \"$ORIGINAL_PACKAGE_NAME\"",
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
