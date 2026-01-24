package app.revanced.patches.music.misc.settings

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.googleApiActivityMethod by gettingFirstMutableMethodDeclaratively {
    name("onCreate")
    definingClass { endsWith("GoogleApiActivity;") }
    returnType("V")
    parameterTypes("Landroid/os/Bundle;")
}
