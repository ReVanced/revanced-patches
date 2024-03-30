package app.revanced.patches.music.layout.upgradebutton

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.extensions.newLabel
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.toInstructions
import app.revanced.patches.music.layout.upgradebutton.fingerprints.PivotBarConstructorFingerprint
import app.revanced.util.exception
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction22t
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Patch(
    name = "Remove upgrade button",
    description = "Removes the upgrade tab from the pivot bar.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")],
)
@Suppress("unused")
object RemoveUpgradeButtonPatch : BytecodePatch(
    setOf(PivotBarConstructorFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        PivotBarConstructorFingerprint.result?.let {
            it.mutableMethod.apply {
                val pivotBarElementFieldReference = getInstruction(it.scanResult.patternScanResult!!.endIndex - 1)
                    .getReference<FieldReference>()

                val register = (getInstructions().first() as Instruction35c).registerC

                // First compile all the needed instructions.
                val instructionList = """
                    invoke-interface { v0 }, Ljava/util/List;->size()I
                    move-result v1
                    const/4 v2, 0x4
                    invoke-interface {v0, v2}, Ljava/util/List;->remove(I)Ljava/lang/Object;
                    iput-object v0, v$register, $pivotBarElementFieldReference
                """.toInstructions().toMutableList()

                val endIndex = it.scanResult.patternScanResult!!.endIndex

                // Replace the instruction to retain the label at given index.
                replaceInstruction(
                    endIndex - 1,
                    instructionList[0], // invoke-interface.
                )
                // Do not forget to remove this instruction since we added it already.
                instructionList.removeFirst()

                val exitInstruction = instructionList.last() // iput-object
                addInstruction(
                    endIndex,
                    exitInstruction,
                )
                // Do not forget to remove this instruction since we added it already.
                instructionList.removeLast()

                // Add the necessary if statement to remove the upgrade tab button in case it exists.
                instructionList.add(
                    2, // if-le.
                    BuilderInstruction22t(
                        Opcode.IF_LE,
                        1,
                        2,
                        newLabel(endIndex),
                    ),
                )

                addInstructions(
                    endIndex,
                    instructionList,
                )
            }
        } ?: throw PivotBarConstructorFingerprint.exception
    }
}
