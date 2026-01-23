package app.revanced.patches.youtube.video.speed.remember

import app.revanced.patcher.addString
import app.revanced.patcher.fingerprint

internal val initializePlaybackSpeedValuesFingerprint = fingerprint {
    parameterTypes("[L", "I")
    instructions(
        addString("menu_item_playback_speed"),
    )
}
