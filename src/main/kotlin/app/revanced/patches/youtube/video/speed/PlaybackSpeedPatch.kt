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
                "18.38.44",
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
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