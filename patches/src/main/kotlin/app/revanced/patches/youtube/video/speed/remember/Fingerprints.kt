package app.revanced.patches.youtube.video.speed.remember

import app.revanced.patcher.fingerprint
import app.revanced.patcher.string

internal val initializePlaybackSpeedValuesFingerprint = fingerprint {
    parameters("[L", "I")
    instructions(
        string("menu_item_playback_speed"),
    )
}
