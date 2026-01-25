package app.revanced.patches.youtube.video.speed.remember

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.initializePlaybackSpeedValuesMethod by gettingFirstMethodDeclaratively {
    parameterTypes("[L", "I")
    instructions(
        "menu_item_playback_speed"(),
    )
}
