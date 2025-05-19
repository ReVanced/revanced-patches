package app.revanced.patches.cricbuzz

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction11x

@Suppress("unused")
val disableAdsPatch = bytecodePatch (
    "Disable ads",
) {
    compatibleWith("com.cricbuzz.android")

    execute {
        // modify user state as "ACTIVE" during switch statement
        userStateSwitchFingerprint.method.apply {
            // find the line where the switch variable is assigned
            // and replace the register with a constant string "ACTIVE"
            val lineFingerprint = fingerprint {
                opcodes(Opcode.MOVE_RESULT_OBJECT)
            }
            val insertIndex = lineFingerprint.match(userStateSwitchFingerprint.method).patternMatch!!.startIndex
            val register = getInstruction<Instruction11x>(insertIndex).registerA

            addInstructions(
                insertIndex + 1,
                "const-string v$register, \"ACTIVE\""
            )
        }
    }
}
