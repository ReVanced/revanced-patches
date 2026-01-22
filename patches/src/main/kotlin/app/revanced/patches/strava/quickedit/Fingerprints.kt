package app.revanced.patches.strava.quickedit

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.getHasAccessToQuickEditMethod by gettingFirstMutableMethodDeclaratively {
    name("getHasAccessToQuickEdit")
    returnType("Z")
}
