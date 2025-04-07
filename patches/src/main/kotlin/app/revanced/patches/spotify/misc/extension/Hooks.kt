package app.revanced.patches.spotify.misc.extension

import app.revanced.patches.shared.misc.extension.extensionHook

internal val spotifyMainActivityOnCreate = extensionHook {
    custom { method, classDef ->
        method.name == "onCreate" && (classDef.type == "Lcom/spotify/music/SpotifyMainActivity;"
                || classDef.type == "Lcom/spotify/music/MainActivity;") // target 8.6.98.900
    }
}
