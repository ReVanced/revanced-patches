package app.revanced.patches.instagram.misc.selectableBio

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.instagram.misc.selectableBio.fingerprints.SelectableBioFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c

@Patch(
    name = "Selectable bio",
    description = "Make the user's bio selectable",
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
    use = false
)
@Suppress("unused")
object SelectableBioPatch:BytecodePatch(
    setOf(SelectableBioFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        val result = SelectableBioFingerprint.result ?: throw SelectableBioFingerprint.exception

        val method = result.mutableMethod
        val instructions = method.getInstructions()

        //location were bio text is set to a textview
        val loc = instructions.first { it.opcode == Opcode.INVOKE_VIRTUAL }.location.index

        //get registers
        val reg = method.getInstruction<BuilderInstruction35c>(loc)

        //get textview register
        val r1 = reg.registerC
        //get text register
        val r2 = reg.registerD

        //make the textview selectable
        method.addInstructions(loc+1,"""
            const/4 v$r2, 0x1
            invoke-virtual {v$r1, v$r2}, Landroid/widget/TextView;->setTextIsSelectable(Z)V
        """.trimIndent())

    }
}