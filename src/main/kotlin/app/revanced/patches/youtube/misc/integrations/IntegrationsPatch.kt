package app.revanced.patches.youtube.misc.integrations

import app.revanced.patches.shared.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.integrations.hooks.*

val integrationsPatch = integrationsPatch(
    applicationInitHook,
    standalonePlayerActivityHook,
    remoteEmbeddedPlayerHook,
    remoteEmbedFragmentHook,
    embeddedPlayerControlsOverlayHook,
    embeddedPlayerHook,
    apiPlayerServiceHook,
)
