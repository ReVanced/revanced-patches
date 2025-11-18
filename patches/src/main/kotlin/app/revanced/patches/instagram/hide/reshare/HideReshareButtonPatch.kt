package app.revanced.patches.instagram.hide.reshare

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstLiteralInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderSparseSwitchPayload
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused")
val hideReshareButtonPatch = bytecodePatch(
    name = "Hide reshare button",
    description = "Hides the reshare button from both posts and reels.",
    use = false
) {
    compatibleWith("com.instagram.android")

    execute {
        feedResponseMediaParserFingerprint.method.apply {
            // Each json field is parsed in a switch statement, where the case of the switch is the hashed field name.

            // First, find the switch payload where our field of interest is being processed. So find the payload that
            // has a key == to our field of interest.
            val switchPayload = implementation!!.instructions.first { ins ->
                ins.opcode == Opcode.SPARSE_SWITCH_PAYLOAD &&
                        (ins as BuilderSparseSwitchPayload).switchElements.any { it.key == hashedFieldInteger }
            } as BuilderSparseSwitchPayload

            // Get the target label, so find the instruction offset where the switch case is pointing to.
            val switchTargetLabel = switchPayload.switchElements
                .first { it.key == hashedFieldInteger }
                .target

            // From that label, navigate forward until our field of interest is being instantiated.
            val moveResultIndex = indexOfFirstInstructionOrThrow(
                switchTargetLabel.location.index,
                Opcode.MOVE_RESULT_OBJECT
            )

            val moveResultRegister = getInstruction<OneRegisterInstruction>(moveResultIndex).registerA

            addInstruction(
                moveResultIndex + 1,
                "sget-object v$moveResultRegister, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;"
            )
        }

        reelPostsResponseMediaParserFingerprint.method.apply {
            // Each json field is parsed in a series of if statements, where the if comparison is done comparing the hashed field name.

            // Get index of "const v*, -0x207dadd2".
            val switchIndex = indexOfFirstLiteralInstructionOrThrow(hashedFieldInteger.toLong())

            // Here an internal Instagram native library is used to handle settings of experiments, using hash codes of the fields.
            // Find where our field of interest is being instantiated.
            val moveBooleanResultIndex = indexOfFirstInstructionOrThrow(switchIndex) {
                getReference<MethodReference>()?.name == "getOptionalBooleanValueByHashCode"
            } + 1

            val moveBooleanResultRegister = getInstruction<OneRegisterInstruction>(moveBooleanResultIndex).registerA

            addInstruction(
                moveBooleanResultIndex + 1,
                "sget-object v$moveBooleanResultRegister, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;"
            )
        }
    }
}
