package app.revanced.patches.spotify.misc.extension

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch
import app.revanced.patches.spotify.shared.SPOTIFY_MAIN_ACTIVITY_LEGACY
import java.util.logging.Logger

/**
 * If patching a legacy 8.x target. This may also be set if patching slightly older/newer app targets,
 * but the only legacy target of interest is 8.6.98.900 as it's the last version that
 * supports Spotify integration on Kenwood/Pioneer car stereos.
 */
internal var IS_SPOTIFY_LEGACY_APP_TARGET = false

val sharedExtensionPatch = bytecodePatch {
    dependsOn(sharedExtensionPatch("spotify", mainActivityOnCreateHook))

    execute {
        IS_SPOTIFY_LEGACY_APP_TARGET = mainActivityOnCreateHook.fingerprint
            .originalClassDef.type == SPOTIFY_MAIN_ACTIVITY_LEGACY

        if (IS_SPOTIFY_LEGACY_APP_TARGET) {
            Logger.getLogger(this::class.java.name).warning(
                "Patching a legacy Spotify version. Patch functionality may be limited."
            )
        }
    }
}
