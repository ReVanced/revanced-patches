package app.revanced.patches.bandcamp.limitations

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val removePlayLimitsPatch = bytecodePatch(
    name = "Remove play limits",
    description = "Disables purchase nagging and playback limits of not purchased tracks.",
) {
    compatibleWith("com.bandcamp.android")

    execute {
        handlePlaybackLimitsFingerprint.method.returnEarly()
    }
}
