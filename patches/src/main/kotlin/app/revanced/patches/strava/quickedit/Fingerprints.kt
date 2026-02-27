package app.revanced.patches.strava.quickedit

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.getHasAccessToQuickEditMethod by gettingFirstMethodDeclaratively {
    name("getHasAccessToQuickEdit")
    returnType("Z")
}
