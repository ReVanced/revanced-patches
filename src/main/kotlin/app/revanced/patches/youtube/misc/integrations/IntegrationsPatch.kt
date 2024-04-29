package app.revanced.patches.youtube.misc.integrations

import app.revanced.patches.shared.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.integrations.fingerprints.*

val integrationsPatch = integrationsPatch(
    applicationInitFingerprint,
    standalonePlayerActivityFingerprint,
    remoteEmbeddedPlayerFingerprint,
    remoteEmbedFragmentFingerprint,
    embeddedPlayerControlsOverlayFingerprint,
    embeddedPlayerFingerprint,
    apiPlayerServiceFingerprint,
)
