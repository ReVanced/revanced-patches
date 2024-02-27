package app.revanced.patches.music.utils.flyoutbutton

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.utils.flyoutbutton.fingerprints.FlyoutPanelLikeButtonFingerprint
import app.revanced.patches.music.utils.integrations.Constants.FLYOUT
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch.MusicMenuLikeButtons
import app.revanced.util.exception
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    dependencies = [
        FlyoutButtonContainerResourcePatch::class,
        SharedResourceIdPatch::class
    ]
)
object FlyoutButtonContainerPatch : BytecodePatch(
    setOf(FlyoutPanelLikeButtonFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        FlyoutPanelLikeButtonFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = getWideLiteralInstructionIndex(MusicMenuLikeButtons)

                var insertIndex = -1

                for (index in targetIndex until targetIndex + 5) {
                    if (getInstruction(index).opcode != Opcode.MOVE_RESULT_OBJECT) continue

                    val register = getInstruction<OneRegisterInstruction>(index).registerA
                    insertIndex = index

                    addInstruction(
                        index + 1,
                        "invoke-static {v$register}, $FLYOUT->setFlyoutButtonContainer(Landroid/view/View;)V"
                    )
                    break
                }
                if (insertIndex == -1)
                    throw PatchException("Couldn't find target Index")
            }
        } ?: throw FlyoutPanelLikeButtonFingerprint.exception

    }
}