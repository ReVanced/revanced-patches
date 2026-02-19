package app.revanced.patches.music.misc.gms

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.musicActivityOnCreateMethod by gettingFirstMethodDeclaratively {
    name("onCreate")
    definingClass { endsWith("/MusicActivity;") }
    returnType("V")
    parameterTypes("Landroid/os/Bundle;")
}
