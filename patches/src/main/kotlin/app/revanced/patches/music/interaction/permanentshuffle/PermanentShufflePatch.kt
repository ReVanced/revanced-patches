package app.revanced.patches.music.interaction.permanentshuffle

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val permanentShufflePatch = bytecodePatch(
    name = "Permanent shuffle",
    description = "Permanently remember your shuffle preference " +
        "even if the playlist ends or another track is played.",
    use = false,
) {
    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "6.45.54",
            "6.51.53",
            "7.01.53",
            "7.02.52",
            "7.03.52",
        ),
    )

    execute {
        disableShuffleFingerprint.method().addInstruction(0, "return-void")
    }
}
