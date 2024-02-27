package app.revanced.patches.youtube.utils.playerbutton

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.utils.integrations.Constants.PLAYER
import app.revanced.patches.youtube.utils.playerbutton.fingerprints.LiveChatFingerprint
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.util.exception
import app.revanced.util.findMutableMethodOf
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c

@Patch(dependencies = [SharedResourceIdPatch::class])
object PlayerButtonHookPatch : BytecodePatch(
    setOf(LiveChatFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        LiveChatFingerprint.result?.let {
            val endIndex = it.scanResult.patternScanResult!!.endIndex
            val instructions = it.mutableMethod.getInstruction(endIndex)
            val imageButtonClass =
                context
                    .findClass(
                        (instructions as BuilderInstruction21c)
                            .reference.toString()
                    )!!
                    .mutableClass

            for (method in imageButtonClass.methods) {
                imageButtonClass.findMutableMethodOf(method).apply {
                    var jumpInstruction = true

                    implementation!!.instructions.forEachIndexed { index, instructions ->
                        if (instructions.opcode == Opcode.INVOKE_VIRTUAL) {
                            val definedInstruction = (instructions as? BuilderInstruction35c)

                            if (definedInstruction?.reference.toString() ==
                                "Landroid/view/View;->setVisibility(I)V"
                            ) {

                                jumpInstruction = !jumpInstruction
                                if (jumpInstruction) return@forEachIndexed

                                val firstRegister = definedInstruction?.registerC
                                val secondRegister = definedInstruction?.registerD

                                addInstructions(
                                    index, """
                                        invoke-static {v$firstRegister, v$secondRegister}, $PLAYER->hidePlayerButton(Landroid/view/View;I)I
                                        move-result v$secondRegister
                                        """
                                )
                            }
                        }
                    }
                }
            }
        } ?: throw LiveChatFingerprint.exception

    }
}