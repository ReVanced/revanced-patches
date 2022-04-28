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
import org.jf.dexlib2.builder.instruction.BuilderInstruction22t
import org.jf.dexlib2.iface.instruction.formats.Instruction22c
import org.jf.dexlib2.iface.instruction.formats.Instruction35c

private val compatiblePackages = listOf(
    PackageMetadata(
        "com.google.android.apps.youtube.music",
        listOf("5.03.50")
    )
)

class RemoveUpgradeTabPatch : Patch(
    PatchMetadata(
        "upgrade-tab-remover",
        "Remove Upgrade Tab Patch",
        "Remove the upgrade tab from t he pivot bar in YouTube music.",
        compatiblePackages,
        "0.0.1"
    ),
    listOf(
        MethodSignature(
            MethodSignatureMetadata(
                "pivot-bar-constructor",
                MethodMetadata("Lhfu;", "<init2>"), // unknown
                PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages,
                "Required signature for this patch.",
                "0.0.1"
            ),
            "V",
            AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
            listOf("L", "Z"),
            listOf(
                Opcode.INVOKE_DIRECT,
                Opcode.CONST_4,
                Opcode.IPUT_OBJECT,
                Opcode.IPUT_OBJECT,
                Opcode.IPUT_BOOLEAN,
                Opcode.IGET_OBJECT,
                Opcode.IF_NEZ,
                Opcode.GOTO,
                Opcode.NEW_INSTANCE,
                Opcode.INVOKE_DIRECT,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT,
                Opcode.IF_EQZ,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.IGET,
                Opcode.CONST,
                Opcode.IF_NE,
                Opcode.IGET_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.GOTO,
                Opcode.SGET_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT,
                Opcode.IF_EQZ,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.IGET,
                Opcode.CONST,
                Opcode.IF_NE,
                Opcode.IGET_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.INVOKE_INTERFACE,
                Opcode.GOTO,
                Opcode.NOP,
                Opcode.IPUT_OBJECT,
                Opcode.RETURN_VOID
            )
        )
    )
) {
    override fun execute(patcherData: PatcherData): PatchResult {
        val result = signatures.first().result!!
        val implementation = result.method.implementation!!

        val pivotBarElementFieldRef =
            (implementation.instructions[result.scanData.endIndex - 1] as Instruction22c).reference

        val register = (implementation.instructions.first() as Instruction35c).registerC
        // first compile all the needed instructions
        val instructionList =
            """
                invoke-interface { v0 }, Ljava/util/List;->size()I
                move-result v1
                const/4 v2, 0x3
                invoke-interface {v0, v2}, Ljava/util/List;->remove(I)Ljava/lang/Object;
                iput-object v0, v$register, $pivotBarElementFieldRef
            """.trimIndent().toInstructions().toMutableList()


        // replace the instruction to retain the label at given index
        implementation.replaceInstruction(
            result.scanData.endIndex - 1,
            instructionList[0] // invoke-interface
        )
        // do not forget to remove this instruction since we added it already
        instructionList.removeFirst()

        val exitInstruction = instructionList.last() // iput-object
        implementation.addInstruction(
            result.scanData.endIndex,
            exitInstruction
        )
        // do not forget to remove this instruction since we added it already
        instructionList.removeLast()

        // add the necessary if statement to remove the upgrade tab button in case it exists
        instructionList.add(
            2, // if-le
            BuilderInstruction22t(
                Opcode.IF_LE,
                1, 2,
                implementation.newLabelForIndex(result.scanData.endIndex)
            )
        )

        implementation.addInstructions(
            result.scanData.endIndex,
            instructionList
        )
        return PatchResultSuccess()
    }
}