package app.revanced.patches.instagram.patches.selectableBio

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.instagram.patches.selectableBio.fingerprints.SelectableBioFingerprint
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c

@Patch(
    name = "Selectable bio",
    description = "Make the user's bio selectable",
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
    use = false,
)
@Suppress("unused")
object SelectableBioPatch : BytecodePatch(
    setOf(SelectableBioFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        SelectableBioFingerprint.resultOrThrow().let { it ->
            it.mutableMethod.apply {
                // location where bio text is set to a textview.
                val index = getInstructions().first { it.opcode == Opcode.INVOKE_VIRTUAL }.location.index

                // get reference of the setTextView method
                val methodRef = getInstruction<BuilderInstruction35c>(index)

                // get the textview register.
                val textViewRegister = methodRef.registerC
                // get the text register (needed to set selectable boolean).
                val textRegister = methodRef.registerD

                // make the textview selectable.
                addInstructions(
                    index + 1,
                    """
                    const/4 v$textRegister, 0x1
                    invoke-virtual {v$textViewRegister, v$textRegister}, Landroid/widget/TextView;->setTextIsSelectable(Z)V
                    """.trimIndent(),
                )
            }
        }
    }
}
