package app.revanced.patches.youtube.video.speed

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.video.speed.custom.CustomPlaybackSpeedPatch
import app.revanced.patches.youtube.video.speed.remember.RememberPlaybackSpeedPatch

@Patch(
    name = "Playback speed",
    description = "Adds options to customize available playback speeds and to remember the last playback speed selected.",
    dependencies = [CustomPlaybackSpeedPatch::class, RememberPlaybackSpeedPatch::class],
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
                "19.11.43"
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
