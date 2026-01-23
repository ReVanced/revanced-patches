package app.revanced.patches.music.audio.exclusiveaudio

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.util.returnEarly

@Suppress("unused")
val `Enable exclusive audio playback` by creatingBytecodePatch(
    description = "Enables the option to play audio without video.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
    )

    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52",
            "8.10.52"
        )
    )

    apply {
        allowExclusiveAudioPlaybackFingerprint.method.returnEarly(true)
    }
}
