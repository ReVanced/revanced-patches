package app.revanced.patches.youtube.video.speed

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.video.speed.button.playbackSpeedButtonPatch
import app.revanced.patches.youtube.video.speed.custom.customPlaybackSpeedPatch
import app.revanced.patches.youtube.video.speed.remember.rememberPlaybackSpeedPatch

@Suppress("unused")
val playbackSpeedPatch = bytecodePatch(
    name = "Playback speed",
    description = "Adds options to customize available playback speeds, remember the last playback speed selected " +
        "and show a speed dialog button in the video player.",
) {
    dependsOn(
        playbackSpeedButtonPatch,
        customPlaybackSpeedPatch,
        rememberPlaybackSpeedPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.45.38",
            "19.46.42",
            "19.47.53",
        ),
    )
}
