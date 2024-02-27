package app.revanced.patches.youtube.utils.returnyoutubedislike.oldlayout

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.utils.integrations.Constants.UTILS_PATH
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.DislikeButton
import app.revanced.patches.youtube.utils.returnyoutubedislike.oldlayout.fingerprints.ButtonTagFingerprint
import app.revanced.util.exception
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(dependencies = [SharedResourceIdPatch::class])
object ReturnYouTubeDislikeOldLayoutPatch : BytecodePatch(
    setOf(ButtonTagFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        ButtonTagFingerprint.result?.let {
            it.mutableMethod.apply {
                val dislikeButtonIndex = getWideLiteralInstructionIndex(DislikeButton)

                val resourceIdentifierRegister =
                    getInstruction<OneRegisterInstruction>(dislikeButtonIndex).registerA
                val textViewRegister =
                    getInstruction<OneRegisterInstruction>(dislikeButtonIndex + 4).registerA

                addInstruction(
                    dislikeButtonIndex + 4,
                    "invoke-static {v$resourceIdentifierRegister, v$textViewRegister}, $INTEGRATIONS_RYD_CLASS_DESCRIPTOR->setOldUILayoutDislikes(ILandroid/widget/TextView;)V"
                )
            }
        } ?: throw ButtonTagFingerprint.exception

    }

    private const val INTEGRATIONS_RYD_CLASS_DESCRIPTOR =
        "$UTILS_PATH/ReturnYouTubeDislikePatch;"
}
