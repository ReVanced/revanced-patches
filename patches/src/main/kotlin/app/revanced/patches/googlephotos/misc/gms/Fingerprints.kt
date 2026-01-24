package app.revanced.patches.googlephotos.misc.gms

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.homeActivityOnCreateMethod by gettingFirstMutableMethodDeclaratively {
    name("onCreate")
    definingClass { endsWith("/HomeActivity;") }
}
