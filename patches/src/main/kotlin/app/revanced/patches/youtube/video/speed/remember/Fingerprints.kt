package app.revanced.patches.youtube.video.speed.remember

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.initializePlaybackSpeedValuesMethod by gettingFirstMutableMethodDeclaratively(
    "menu_item_playback_speed",
) {
    parameterTypes("[L", "I")
}
