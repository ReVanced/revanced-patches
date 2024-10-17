package app.revanced.patches.instagram.patches.interaction.bio

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.instagram.patches.interaction.bio.fingerprints.SelectableBioFingerprint
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Patch(
    name = "Selectable bio",
    description = "Make the user's bio selectable.",
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
)
@Suppress("unused")
object SelectableBioPatch : BytecodePatch(
    setOf(SelectableBioFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        SelectableBioFingerprint.resultOrThrow().mutableMethod.apply {
            val setBioTextIndex = getInstructions().first { it.opcode == Opcode.INVOKE_VIRTUAL }.location.index
            val setTextViewInstruction = getInstruction<FiveRegisterInstruction>(setBioTextIndex)
            val textViewRegister = setTextViewInstruction.registerC
            val textRegister = setTextViewInstruction.registerD

            // Make the textview selectable.
            addInstructions(
                setBioTextIndex + 1,
                """
                    const/4 v$textRegister, 0x1
                    invoke-virtual { v$textViewRegister, v$textRegister }, Landroid/widget/TextView;->setTextIsSelectable(Z)V
                """,
            )
        }
    }
}
