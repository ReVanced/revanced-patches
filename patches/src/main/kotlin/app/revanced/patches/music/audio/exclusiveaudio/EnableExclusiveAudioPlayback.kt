package app.revanced.patches.music.audio.exclusiveaudio

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val enableExclusiveAudioPlaybackPatch = bytecodePatch(
    name = "Enable exclusive audio playback",
    description = "Enables the option to play audio without video.",
) {
    compatibleWith("com.google.android.apps.youtube.music")

    execute {
        allowExclusiveAudioPlaybackFingerprint.method().apply {
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """,
            )
        }
    }
}
