package app.revanced.patches.music.interaction.permanentshuffle.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.music.annotations.MusicCompatibility
import app.revanced.patches.music.interaction.permanentshuffle.fingerprints.DisableShuffleFingerprint

@Patch(false)
@Name("Permanent shuffle")
@Description("Permanently remember your shuffle preference " +
        "even if the playlist ends or another track is played.")
@MusicCompatibility
class PermanentShuffleTogglePatch : BytecodePatch(
    listOf(DisableShuffleFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        DisableShuffleFingerprint.result?.mutableMethod?.addInstruction(0, "return-void")
            ?: throw DisableShuffleFingerprint.toErrorResult()
    }
}
