package app.revanced.patches.youtube.video.speed

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.video.speed.button.PlaybackSpeedButtonPatch
import app.revanced.patches.youtube.video.speed.custom.CustomPlaybackSpeedPatch
import app.revanced.patches.youtube.video.speed.remember.RememberPlaybackSpeedPatch

@Patch(
    name = "Playback speed",
    description = "Adds options to customize available playback speeds, remember the last playback speed selected " +
            "and show a speed dialog button to the video player.",
    dependencies = [
        PlaybackSpeedButtonPatch::class,
        CustomPlaybackSpeedPatch::class,
        RememberPlaybackSpeedPatch::class,
     ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43",
                "19.12.41",
                "19.13.37",
                "19.14.43",
                "19.15.36",
                "19.16.39",
                "19.17.41",
                "19.18.41",
                "19.19.39",
                "19.20.35",
                "19.21.40",
                "19.22.43",
                "19.23.40",
                "19.24.45",
                "19.25.37", 
                "19.26.42",
                "19.28.42",
                "19.29.42",
                "19.30.39",
            ],
        ),
    ],
)
@Suppress("unused")
object PlaybackSpeedPatch : BytecodePatch(emptySet()) {
    override fun execute(context: BytecodeContext) {
        // All patches this patch depends on succeed.
    }
}