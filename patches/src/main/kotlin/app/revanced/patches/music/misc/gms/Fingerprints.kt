package app.revanced.patches.music.misc.gms

import app.revanced.patcher.*
import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext


internal val BytecodePatchContext.musicActivityOnCreateMethod by gettingFirstMutableMethodDeclaratively {
    name("onCreate")
    definingClass("/MusicActivity;"::endsWith)
    returnType("V")
    parameterTypes("Landroid/os/Bundle;")
}