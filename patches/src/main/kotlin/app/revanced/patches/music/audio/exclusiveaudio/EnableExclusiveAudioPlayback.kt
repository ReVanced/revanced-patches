package app.revanced.patches.music.audio.exclusiveaudio

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val enableExclusiveAudioPlaybackPatch = bytecodePatch(
    name = "Enable exclusive audio playback",
    description = "Enables the option to play audio without video.",
) {
    compatibleWith("com.google.android.apps.youtube.music")

    execute {
        allowExclusiveAudioPlaybackFingerprint.method.returnEarly(true)
    }
}
