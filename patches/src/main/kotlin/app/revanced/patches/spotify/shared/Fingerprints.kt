package app.revanced.patches.spotify.shared

import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

private const val SPOTIFY_MAIN_ACTIVITY = "Lcom/spotify/music/SpotifyMainActivity;"

/**
 * Main activity of target 8.6.98.900.
 */
internal const val SPOTIFY_MAIN_ACTIVITY_LEGACY = "Lcom/spotify/music/MainActivity;"

internal val mainActivityOnCreateFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Landroid/os/Bundle;")
    custom { method, classDef ->
        method.name == "onCreate" && (classDef.type == SPOTIFY_MAIN_ACTIVITY
                || classDef.type == SPOTIFY_MAIN_ACTIVITY_LEGACY)
    }
}

private var isLegacyAppTarget: Boolean? = null

/**
 * If patching a legacy 8.x target. This may also be set if patching slightly older/newer app targets,
 * but the only legacy target of interest is 8.6.98.900 as it's the last version that
 * supports Spotify integration on Kenwood/Pioneer car stereos.
 */
context(BytecodePatchContext)
internal val IS_SPOTIFY_LEGACY_APP_TARGET get(): Boolean {
    if (isLegacyAppTarget == null) {
        isLegacyAppTarget = mainActivityOnCreateFingerprint.originalClassDef.type == SPOTIFY_MAIN_ACTIVITY_LEGACY
    }
    return isLegacyAppTarget!!
}
