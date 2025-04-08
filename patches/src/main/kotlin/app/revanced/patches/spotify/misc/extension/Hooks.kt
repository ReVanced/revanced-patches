package app.revanced.patches.spotify.misc.extension

import app.revanced.patches.shared.misc.extension.extensionHook

private const val SPOTIFY_MAIN_ACTIVITY = "Lcom/spotify/music/SpotifyMainActivity;"

/**
 * Main activity of target 8.6.98.900.
 */
internal const val SPOTIFY_MAIN_ACTIVITY_LEGACY = "Lcom/spotify/music/MainActivity;"

internal val spotifyMainActivityOnCreate = extensionHook {
    custom { method, classDef ->
        method.name == "onCreate" && (classDef.type == SPOTIFY_MAIN_ACTIVITY
                || classDef.type == SPOTIFY_MAIN_ACTIVITY_LEGACY)
    }
}
