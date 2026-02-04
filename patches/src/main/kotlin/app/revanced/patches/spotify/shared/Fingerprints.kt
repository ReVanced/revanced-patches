package app.revanced.patches.spotify.shared

import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

private const val SPOTIFY_MAIN_ACTIVITY = "Lcom/spotify/music/SpotifyMainActivity;"

internal val BytecodePatchContext.mainActivityOnCreateMethod by gettingFirstMethodDeclaratively {
    name("onCreate")
    definingClass(SPOTIFY_MAIN_ACTIVITY)
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Landroid/os/Bundle;")
}
