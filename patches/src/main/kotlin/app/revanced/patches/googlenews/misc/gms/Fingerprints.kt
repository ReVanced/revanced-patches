package app.revanced.patches.googlenews.misc.gms

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.magazinesActivityOnCreateMethod by gettingFirstMethodDeclaratively {
    name("onCreate")
    definingClass { endsWith("/StartActivity;") }
}
