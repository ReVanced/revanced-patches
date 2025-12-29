package app.revanced.patches.youtube.video.speed.remember

import app.revanced.patcher.fingerprint
import app.revanced.patcher.addString

internal val initializePlaybackSpeedValuesFingerprint = fingerprint {
    parameters("[L", "I")
    instructions(
        addString("menu_item_playback_speed"),
    )
}
