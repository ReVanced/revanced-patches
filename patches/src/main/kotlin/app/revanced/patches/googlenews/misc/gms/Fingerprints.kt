package app.revanced.patches.googlenews.misc.gms

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.magazinesActivityOnCreateMethod by gettingFirstMutableMethodDeclaratively {
    name("onCreate")
    definingClass { endsWith("/StartActivity;") }
}
