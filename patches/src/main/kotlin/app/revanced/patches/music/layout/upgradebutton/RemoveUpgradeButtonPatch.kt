package app.revanced.patches.music.layout.upgradebutton

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.extensions.newLabel
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.toInstructions
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction22t
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Suppress("unused")
val removeUpgradeButtonPatch = bytecodePatch(
    name = "Remove upgrade button",
    description = "Removes the upgrade tab from the pivot bar.",
) {
    compatibleWith("com.google.android.apps.youtube.music")

    execute {
        pivotBarConstructorFingerprint.method.apply {
            val pivotBarElementFieldReference =
                getInstruction(pivotBarConstructorFingerprint.filterMatches.last().index - 1)
                    .getReference<FieldReference>()

            val register = getInstruction<FiveRegisterInstruction>(0).registerC

            // First compile all the needed instructions.
            val instructionList = """
                invoke-interface { v0 }, Ljava/util/List;->size()I
                move-result v1
                const/4 v2, 0x4
                invoke-interface {v0, v2}, Ljava/util/List;->remove(I)Ljava/lang/Object;
                iput-object v0, v$register, $pivotBarElementFieldReference
            """.toInstructions().toMutableList()

            val endIndex = pivotBarConstructorFingerprint.filterMatches.last().index

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
    }
}
