package app.revanced.patches.spotify.misc.extension

import app.revanced.patches.shared.misc.extension.extensionHook

internal val spotifyMainActivityOnCreate = extensionHook {
    custom { method, classDef ->
        classDef.type == "Lcom/spotify/music/SpotifyMainActivity;" &&
                method.name == "onCreate"
    }
}
