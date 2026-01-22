package app.revanced.patches.instagram.ghost.story

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.setMediaSeenHashmapMethod by gettingFirstMutableMethodDeclaratively("media/seen/") {
    parameterTypes()
    returnType("V")
}
