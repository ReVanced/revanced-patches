package app.revanced.patches.all.directory

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.all.misc.transformation.BaseTransformInstructionsPatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil

@Patch(
    name = "Change data directory location",
    description = "Changes the data directory in the application from " +
        "the app internal storage directory to /sdcard/android/data accessible by root-less devices." +
        "Using this patch can cause unexpected issues with some apps.",
    use = false,
)
@Suppress("unused")
object ChangeDataDirectoryLocationPatch : BaseTransformInstructionsPatch<Int>() {
    override fun filterMap(
        classDef: ClassDef,
        method: Method,
        instruction: Instruction,
        instructionIndex: Int,
    ): Int? {
        val reference = instruction.getReference<MethodReference>() ?: return null

        if (!MethodUtil.methodSignaturesMatch(reference, MethodCall.GetDir.reference)) {
            return null
        }

        return instructionIndex
    }

    override fun transform(
        mutableMethod: MutableMethod,
        entry: Int,
    ) = transformMethodCall(entry, mutableMethod)

    private fun transformMethodCall(
        instructionIndex: Int,
        mutableMethod: MutableMethod,
    ) {
        val getDirInstruction = mutableMethod.getInstruction<Instruction35c>(instructionIndex)
        val contextRegister = getDirInstruction.registerC
        val dataRegister = getDirInstruction.registerD

        mutableMethod.replaceInstruction(
            instructionIndex,
            "invoke-virtual { v$contextRegister, v$dataRegister }, " +
                "Landroid/content/Context;->getExternalFilesDir(Ljava/lang/String;)Ljava/io/File;",
        )
    }

    private enum class MethodCall(
        val reference: MethodReference,
    ) {
        GetDir(
            ImmutableMethodReference(
                "Landroid/content/Context;",
                "getDir",
                listOf("Ljava/lang/String;", "I"),
                "Ljava/io/File;",
            ),
        ),
    }
}
