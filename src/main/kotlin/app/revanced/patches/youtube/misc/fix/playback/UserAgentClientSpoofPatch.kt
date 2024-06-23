package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.transformation.BaseTransformInstructionsPatch
import app.revanced.patches.all.misc.transformation.IMethodCall
import app.revanced.patches.all.misc.transformation.Instruction35cInfo
import app.revanced.patches.all.misc.transformation.filterMapInstruction35c
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

object UserAgentClientSpoofPatch : BaseTransformInstructionsPatch<Instruction35cInfo>() {
    private const val ORIGINAL_PACKAGE_NAME = "com.google.android.youtube"
    private const val USER_AGENT_STRING_BUILDER_APPEND_METHOD_REFERENCE =
        "Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;"

    override fun filterMap(
        classDef: ClassDef,
        method: Method,
        instruction: Instruction,
        instructionIndex: Int,
    ) = filterMapInstruction35c<MethodCall>(
        "Lapp/revanced/integrations",
        classDef,
        instruction,
        instructionIndex,
    )

    override fun transform(mutableMethod: MutableMethod, entry: Instruction35cInfo) {
        val (_, _, instructionIndex) = entry

        // Replace the result of context.getPackageName(), if it is used in a user agent string.
        mutableMethod.apply {
            // After context.getPackageName() the result is moved to a register.
            val targetRegister = (
                getInstruction(instructionIndex + 1)
                    as? OneRegisterInstruction ?: return
                ).registerA

            // IndexOutOfBoundsException is possible here,
            // but no such occurrences are present in the app.
            val referee = getInstruction(instructionIndex + 2).getReference<MethodReference>()?.toString()

            // Only replace string builder usage.
            if (referee != USER_AGENT_STRING_BUILDER_APPEND_METHOD_REFERENCE) {
                return
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
                return
            }

            // Overwrite the result of context.getPackageName() with the original package name.
            replaceInstruction(
                instructionIndex + 1,
                "const-string v$targetRegister, \"$ORIGINAL_PACKAGE_NAME\"",
            )
        }
    }

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
}
