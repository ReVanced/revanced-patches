package app.revanced.patches.music.misc.tastebuilder

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.misc.tastebuilder.fingerprints.TasteBuilderConstructorFingerprint
import app.revanced.patches.music.misc.tastebuilder.fingerprints.TasteBuilderSyntheticFingerprint
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch.MusicTastebuilderShelf
import app.revanced.util.exception
import app.revanced.util.getTargetIndex
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide taste builder",
    description = "Hides the \"Tell us which artists you like\" card from the homepage.",
    dependencies = [SharedResourceIdPatch::class],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object TasteBuilderPatch : BytecodePatch(
    setOf(TasteBuilderConstructorFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        TasteBuilderConstructorFingerprint.result?.let { parentResult ->
            TasteBuilderSyntheticFingerprint.resolve(context, parentResult.classDef)

            parentResult.mutableMethod.apply {
                val freeRegister = implementation!!.registerCount - parameters.size - 2
                val constIndex = getWideLiteralInstructionIndex(MusicTastebuilderShelf)
                val targetIndex = getTargetIndex(constIndex, Opcode.MOVE_RESULT_OBJECT)
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstructions(
                    targetIndex + 1, """
                        const/16 v$freeRegister, 0x8
                        invoke-virtual {v$targetRegister, v$freeRegister}, Landroid/view/View;->setVisibility(I)V
                        """
                )
            }
        } ?: throw TasteBuilderConstructorFingerprint.exception

        TasteBuilderSyntheticFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.startIndex
                val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstruction(
                    insertIndex,
                    "const/4 v$insertRegister, 0x0"
                )
            }
        } ?: throw TasteBuilderSyntheticFingerprint.exception
    }
}
