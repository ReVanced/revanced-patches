package app.revanced.patches.music.layout

import app.revanced.patcher.PatcherData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.*
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

class RemoveTastebuilderPatch : Patch(
    PatchMetadata(
        "tastebuilder-remover",
        "Remove Tastebuilder Patch",
        "Remove the tastebuilder from the Home screen.",
        compatiblePackages,
        "0.0.1"
    ),
    listOf(
        MethodSignature(
            MethodSignatureMetadata(
                "tastebuilder-constructor",
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
                Opcode.IPUT_OBJECT
            )
        )
    )
) {
    override fun execute(patcherData: PatcherData): PatchResult {
        val result = signatures.first().result!!
        val implementation = result.method.implementation!!

        val register = (implementation.instructions[result.scanData.endIndex] as Instruction22c).registerA

        val instructionList =
            """
                const/16 v1, 0x8
                invoke-virtual {v${register}, v1}, Landroid/view/View;->setVisibility(I)V
            """.trimIndent().toInstructions().toMutableList()

        implementation.addInstructions(
            result.scanData.endIndex,
            instructionList
        )

        return PatchResultSuccess()
    }
}
