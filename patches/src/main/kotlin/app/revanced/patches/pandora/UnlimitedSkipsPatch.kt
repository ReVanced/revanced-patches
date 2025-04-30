package app.revanced.patches.pandora

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val unlimitedSkipsPatch = bytecodePatch(
    name = "Unlimited skips",
) {
    compatibleWith("com.pandora.android")

    execute {
        constructUserDataFingerprint.method.apply {
            // Last match is "skipLimitBehavior".
            val skipLimitBehaviorStringIndex = constructUserDataFingerprint.stringMatches!!.last().index
            val moveResultObjectIndex =
                indexOfFirstInstructionOrThrow(skipLimitBehaviorStringIndex, Opcode.MOVE_RESULT_OBJECT)
            val skipLimitBehaviorRegister = getInstruction<OneRegisterInstruction>(moveResultObjectIndex).registerA

            addInstruction(
                moveResultObjectIndex + 1,
                "const-string v$skipLimitBehaviorRegister, \"unlimited\""
            )
        }
    }
}
