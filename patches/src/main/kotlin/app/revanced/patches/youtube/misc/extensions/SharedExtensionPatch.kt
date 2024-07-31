package app.revanced.patches.youtube.misc.extensions

import app.revanced.patches.shared.misc.extensions.sharedExtensionPatch
import app.revanced.patches.youtube.misc.extensions.hooks.*

val sharedExtensionPatch = sharedExtensionPatch(
    applicationInitHook,
    standalonePlayerActivityHook,
    remoteEmbeddedPlayerHook,
    remoteEmbedFragmentHook,
    embeddedPlayerControlsOverlayHook,
    embeddedPlayerHook,
    apiPlayerServiceHook,
)
