package app.revanced.patches.instagram.hide.reshare

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val hideReshareButtonPatch = bytecodePatch(
    name = "Hide reshare button",
    description = "Hides the reshare button from both posts and reels.",
    use = false
) {
    compatibleWith("com.instagram.android")

    execute {
        mediaJsonParserFingerprint.method.apply {
            val setCanReshareInstructionIndex = indexOfFirstInstruction(
                mediaJsonParserFingerprint.stringMatches!!.first().index,
                Opcode.MOVE_RESULT_OBJECT
            )

            val canReshareBooleanRegister = getInstruction<OneRegisterInstruction>(setCanReshareInstructionIndex).registerA

            addInstruction(
                setCanReshareInstructionIndex + 1,
                "sget-object v$canReshareBooleanRegister, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;"
            )
        }
    }
}
