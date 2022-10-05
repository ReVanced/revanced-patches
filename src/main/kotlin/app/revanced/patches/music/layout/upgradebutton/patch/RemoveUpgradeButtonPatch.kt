package app.revanced.patches.music.layout.upgradebutton.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.smali.toInstructions
import app.revanced.patches.music.layout.upgradebutton.annotations.RemoveUpgradeButtonCompatibility
import app.revanced.patches.music.layout.upgradebutton.fingerprints.PivotBarConstructorFingerprint
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction22t
import org.jf.dexlib2.iface.instruction.formats.Instruction22c
import org.jf.dexlib2.iface.instruction.formats.Instruction35c


@Patch
@Name("upgrade-button-remover")
@Description("Removes the upgrade tab from the pivot bar.")
@RemoveUpgradeButtonCompatibility
@Version("0.0.1")
class RemoveUpgradeButtonPatch : BytecodePatch(
    listOf(
        PivotBarConstructorFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val result = PivotBarConstructorFingerprint.result!!
        val implementation = result.mutableMethod.implementation!!

        val pivotBarElementFieldRef =
            (implementation.instructions[result.scanResult.patternScanResult!!.endIndex - 1] as Instruction22c).reference

        val register = (implementation.instructions.first() as Instruction35c).registerC
        // first compile all the needed instructions
        val instructionList = """
                invoke-interface { v0 }, Ljava/util/List;->size()I
                move-result v1
                const/4 v2, 0x3
                invoke-interface {v0, v2}, Ljava/util/List;->remove(I)Ljava/lang/Object;
                iput-object v0, v$register, $pivotBarElementFieldRef
            """.toInstructions().toMutableList()


        val endIndex = result.scanResult.patternScanResult!!.endIndex

        // replace the instruction to retain the label at given index
        implementation.replaceInstruction(
            endIndex - 1, instructionList[0] // invoke-interface
        )
        // do not forget to remove this instruction since we added it already
        instructionList.removeFirst()

        val exitInstruction = instructionList.last() // iput-object
        implementation.addInstruction(
            endIndex, exitInstruction
        )
        // do not forget to remove this instruction since we added it already
        instructionList.removeLast()

        // add the necessary if statement to remove the upgrade tab button in case it exists
        instructionList.add(
            2, // if-le
            BuilderInstruction22t(
                Opcode.IF_LE, 1, 2, implementation.newLabelForIndex(endIndex)
            )
        )

        implementation.addInstructions(
            endIndex, instructionList
        )
        return PatchResultSuccess()
    }
}
