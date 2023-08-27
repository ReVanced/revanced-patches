package app.revanced.patches.tiktok.interaction.seekbar.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.tiktok.interaction.seekbar.annotations.ShowSeekbarCompatibility
import app.revanced.patches.tiktok.interaction.seekbar.fingerprints.SetSeekBarShowTypeFingerprint
import org.jf.dexlib2.iface.instruction.formats.Instruction22t

@Patch
@Name("Show seekbar")
@Description("Shows progress bar for all video.")
@ShowSeekbarCompatibility
class ShowSeekbarPatch : BytecodePatch(
    listOf(
        SetSeekBarShowTypeFingerprint,
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SetSeekBarShowTypeFingerprint.result?.mutableMethod?.apply {
            val typeRegister = getInstruction<Instruction22t>(1).registerB

            addInstructions(
                0,
                """
                    const/16 v$typeRegister, 0x64
                """
            )
        } ?: return SetSeekBarShowTypeFingerprint.toErrorResult()
        return PatchResultSuccess()
    }

}