package app.revanced.patches.bandcamp.limitations

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Remove play limits` by creatingBytecodePatch(
    description = "Disables purchase nagging and playback limits of not purchased tracks.",
) {
    compatibleWith("com.bandcamp.android")

    apply {
        handlePlaybackLimitsMethod.returnEarly()
    }
}
