package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val seekbarPatch = bytecodePatch(
    name = "Seekbar",
    description = "Adds an option to disable precise seeking when swiping up on the seekbar, " +
            "an option to slide to seek instead of playing at 2x speed when pressing and holding, " +
            "and an option to tap the player seekbar to seek."
) {
    dependsOn(
        disablePreciseSeekingGesturePatch,
        enableSlideToSeekPatch,
        enableSeekbarTappingPatch,
        seekbarThumbnailsPatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
        )
    )
}
