package app.revanced.patches.spotify.shared

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

private const val SPOTIFY_MAIN_ACTIVITY = "Lcom/spotify/music/SpotifyMainActivity;"

internal val mainActivityOnCreateFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/os/Bundle;")
    custom { method, classDef ->
        method.name == "onCreate" && classDef.type == SPOTIFY_MAIN_ACTIVITY
    }
}
