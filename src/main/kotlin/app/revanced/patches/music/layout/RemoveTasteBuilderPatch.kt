package app.revanced.patches.music.layout

import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.metadata.PackageMetadata
import app.revanced.patcher.patch.implementation.metadata.PatchMetadata
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.signature.MethodMetadata
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.signature.MethodSignatureMetadata
import app.revanced.patcher.signature.PatternScanMethod
import app.revanced.patcher.smali.toInstructions
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.formats.Instruction22c

private val compatiblePackages = listOf(
    PackageMetadata(
        "com.google.android.apps.youtube.music",
        listOf("5.03.50")
    )
)

class RemoveTasteBuilderPatch : BytecodePatch(
    PatchMetadata(
        "tasteBuilder-remover",
        "Remove TasteBuilder Patch",
        "Removes the \"Tell us which artists you like\" card from the Home screen. The same functionality can be triggered from the settings anyway.",
        compatiblePackages,
        "0.0.1"
    ),
    listOf(
        MethodSignature(
            MethodSignatureMetadata(
                "taste-builder-constructor",
                MethodMetadata("Lkyu;", "<init>"),
                PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages,
                "Required signature for this patch.",
                "0.0.1"
            ),
            "V",
            AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
            listOf("L", "L", "L", "L"),
            listOf(
                Opcode.INVOKE_DIRECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.NEW_INSTANCE,
                Opcode.INVOKE_DIRECT,
                Opcode.IPUT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST,
                Opcode.CONST_4,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.CONST,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.IPUT_OBJECT,
                Opcode.NEW_INSTANCE,
                Opcode.INVOKE_DIRECT,
                Opcode.IPUT_OBJECT
            )
        )
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val result = signatures.first().result!!
        val implementation = result.method.implementation!!

        val insertIndex = result.scanData.endIndex - 8

        val register = (implementation.instructions[insertIndex] as Instruction22c).registerA

        val instructionList =
            """
                const/16 v1, 0x8
                invoke-virtual {v${register}, v1}, Landroid/view/View;->setVisibility(I)V
            """.trimIndent().toInstructions().toMutableList()

        implementation.addInstructions(
            insertIndex,
            instructionList
        )

        return PatchResultSuccess()
    }
}
