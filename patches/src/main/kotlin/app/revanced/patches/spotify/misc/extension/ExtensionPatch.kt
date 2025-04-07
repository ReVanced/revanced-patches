package app.revanced.patches.spotify.misc.extension

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch

/**
 * If patching 8.6.98.900.
 */
internal var IS_SPOTIFY_LEGACY_APP_TARGET = false

val sharedExtensionPatch = bytecodePatch {
    dependsOn(sharedExtensionPatch("spotify", spotifyMainActivityOnCreate))

    execute {
        IS_SPOTIFY_LEGACY_APP_TARGET = (classes.find { it.type == SPOTIFY_MAIN_ACTIVITY_LEGACY } != null)
    }
}