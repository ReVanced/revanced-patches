package app.revanced.patches.music.interaction.permanentshuffle

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Deprecated("This patch no longer works and will be removed in the future.")
@Suppress("unused")
val permanentShufflePatch = bytecodePatch(
    description = "Permanently remember your shuffle preference " +
        "even if the playlist ends or another track is played."
) {
    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52",
            "8.10.52"
        )
    )

    execute {
        disableShuffleFingerprint.method.addInstruction(0, "return-void")
    }
}
