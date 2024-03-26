package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.transformation.BaseTransformInstructionsPatch
import app.revanced.patches.all.misc.transformation.IMethodCall
import app.revanced.patches.all.misc.transformation.Instruction35cInfo
import app.revanced.patches.all.misc.transformation.filterMapInstruction35c
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Client spoof",
    description = "Spoofs the client to allow video playback.",
    compatiblePackages = [
        CompatiblePackage("com.google.android.youtube"),
    ],
)
object ClientSpoofPatch : BaseTransformInstructionsPatch<Instruction35cInfo>() {
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

            // IndexOutOfBoundsException is not possible here,
            // but no such occurrences are present in the app.
            val referee = getInstruction(instructionIndex + 2).getReference<MethodReference>()?.toString()

            // This can technically also match non-user agent string builder append methods,
            // but no such occurrences are present in the app.
            if (referee != USER_AGENT_STRING_BUILDER_APPEND_METHOD_REFERENCE) {
                return
            }

            // Overwrite the result of context.getPackageName() with the original package name.
            replaceInstruction(
                instructionIndex + 1,
                "const-string v$targetRegister, \"${ORIGINAL_PACKAGE_NAME}\"",
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
