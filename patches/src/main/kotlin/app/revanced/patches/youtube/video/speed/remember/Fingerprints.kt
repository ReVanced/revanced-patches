package app.revanced.patches.youtube.video.speed.remember

import app.revanced.patcher.accessFlags
import app.revanced.patcher.addString
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.opcodes
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.initializePlaybackSpeedValuesMethod by gettingFirstMethodDeclaratively {
    parameterTypes("[L", "I")
    instructions(
        addString("menu_item_playback_speed"),
    )
}
