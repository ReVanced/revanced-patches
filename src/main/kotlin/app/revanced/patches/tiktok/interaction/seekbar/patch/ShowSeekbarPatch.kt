package app.revanced.patches.tiktok.interaction.seekbar.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.tiktok.interaction.seekbar.annotations.ShowSeekbarCompatibility
import app.revanced.patches.tiktok.interaction.seekbar.fingerprints.SetSeekBarShowTypeFingerprint
import app.revanced.patches.tiktok.interaction.seekbar.fingerprints.ShouldShowSeekBarFingerprint

@Patch
@Name("Show seekbar")
@Description("Shows progress bar for all video.")
@ShowSeekbarCompatibility
class ShowSeekbarPatch : BytecodePatch(
    listOf(
        SetSeekBarShowTypeFingerprint,
        ShouldShowSeekBarFingerprint,
    )
) {
    override fun execute(context: BytecodeContext) {
        ShouldShowSeekBarFingerprint.result?.mutableMethod?.apply {
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        }
        SetSeekBarShowTypeFingerprint.result?.mutableMethod?.apply {
            val typeRegister = implementation!!.registerCount - 1

            addInstructions(
                0,
                """
                    const/16 v$typeRegister, 0x64
                """
            )
        } ?: throw SetSeekBarShowTypeFingerprint.exception
    }

}