package app.revanced.patches.spotify.shared

import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patches.spotify.misc.extension.mainActivityOnCreateHook

private const val SPOTIFY_MAIN_ACTIVITY = "Lcom/spotify/music/SpotifyMainActivity;"

/**
 * Main activity of target 8.6.98.900.
 */
internal const val SPOTIFY_MAIN_ACTIVITY_LEGACY = "Lcom/spotify/music/MainActivity;"

internal val mainActivityOnCreateFingerprint by fingerprint {
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
        isLegacyAppTarget = mainActivityOnCreateHook.invoke()
            .fingerprint.originalClassDef.type == SPOTIFY_MAIN_ACTIVITY_LEGACY
    }
    return isLegacyAppTarget!!
}
