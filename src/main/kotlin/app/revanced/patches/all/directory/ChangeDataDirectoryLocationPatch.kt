package app.revanced.patches.all.directory

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.transformation.BaseTransformInstructionsPatch
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil

@Patch(
    name = "Change data directory location",
    description = "Changes data directory in the application from data/0 to android/data for root-less devices.",
    use = false,
)
@Suppress("unused")
object ChangeDataDirectoryLocationPatch : BaseTransformInstructionsPatch<Int>() {

    override fun filterMap(
        classDef: ClassDef,
        method: Method,
        instruction: Instruction,
        instructionIndex: Int
    ): Int? {
        if (instruction !is ReferenceInstruction) return null

        val reference = instruction.reference as? MethodReference ?: return null

        val match = MethodCall.GetDir.reference.takeIf { 
            MethodUtil.methodSignaturesMatch(reference, it) 
        } ?: return null

        return instructionIndex
    }

    override fun transform(
        mutableMethod: MutableMethod,
        entry: Int
    ) = transformMethodCall(entry, mutableMethod)

    private fun transformMethodCall(
    instructionIndex: Int,
    mutableMethod: MutableMethod
    ) {
        val contextRegister = mutableMethod.getInstruction<Instruction35c>(instructionIndex).registerC
        val dataRegister = mutableMethod.getInstruction<Instruction35c>(instructionIndex).registerD

        mutableMethod.replaceInstruction(
            instructionIndex,
            "invoke-virtual {v$contextRegister, v$dataRegister}, Landroid/content/Context;->getExternalFilesDir(Ljava/lang/String;)Ljava/io/File;"
        )
    }


    private enum class MethodCall(
        val reference: MethodReference
    ) {
        GetDir(
            ImmutableMethodReference(
                "Landroid/content/Context;",
                "getDir",
                listOf("Ljava/lang/String;", "I"),
                "Ljava/io/File;"
            )
        )
    }
}