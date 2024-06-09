package app.revanced.patches.music.interaction.permanentshuffle

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.music.interaction.permanentshuffle.fingerprints.disableShuffleFingerprint

@Suppress("unused")
val permanentShufflePatch = bytecodePatch(
    name = "Permanent shuffle",
    description = "Permanently remember your shuffle preference " +
            "even if the playlist ends or another track is played.",
    use = false,
) {
    val disableShuffleResult by disableShuffleFingerprint

    execute {
        disableShuffleResult.mutableMethod.addInstruction(0, "return-void")
    }
}