package app.revanced.patches.all.misc.directory

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.transformation.transformInstructionsPatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.util.MethodUtil

@Suppress("unused")
val changeDataDirectoryLocationPatch = bytecodePatch(
    name = "Change data directory location",
    description = "Changes the data directory in the application from " +
        "the app internal storage directory to /sdcard/android/data accessible by root-less devices." +
        "Using this patch can cause unexpected issues with some apps.",
    use = false,
) {
    dependsOn(
        transformInstructionsPatch(
            filterMap = filter@{ _, _, instruction, instructionIndex ->
                val reference = instruction.getReference<MethodReference>() ?: return@filter null

                if (!MethodUtil.methodSignaturesMatch(reference, MethodCall.GetDir.reference)) {
                    return@filter null
                }

                return@filter instructionIndex
            },
            transform = { method, index ->
                val getDirInstruction = method.getInstruction<Instruction35c>(index)
                val contextRegister = getDirInstruction.registerC
                val dataRegister = getDirInstruction.registerD

                method.replaceInstruction(
                    index,
                    "invoke-virtual { v$contextRegister, v$dataRegister }, " +
                        "Landroid/content/Context;->getExternalFilesDir(Ljava/lang/String;)Ljava/io/File;",
                )
            },
        ),
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
