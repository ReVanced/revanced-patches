package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val seekbarPatch = bytecodePatch(
    name = "Seekbar",
    description = "Adds options to disable precise seeking when swiping up on the seekbar, " +
            "slide to seek instead of playing at 2x speed when pressing and holding, " +
            "tapping the player seekbar to seek, and hiding the video player seekbar.",
) {
    dependsOn(
        disablePreciseSeekingGesturePatch,
        enableSlideToSeekPatch,
        enableTapToSeekPatch,
        hideSeekbarPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45"
        ),
    )
}
