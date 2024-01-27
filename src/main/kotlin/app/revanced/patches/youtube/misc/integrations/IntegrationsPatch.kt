package app.revanced.patches.youtube.misc.integrations

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch
import app.revanced.patches.youtube.misc.integrations.fingerprints.*

@Patch(requiresIntegrations = true)
object IntegrationsPatch : BaseIntegrationsPatch(
    setOf(
        ApplicationInitFingerprint,
        StandalonePlayerActivityFingerprint,
        RemoteEmbeddedPlayerFingerprint,
        RemoteEmbedFragmentFingerprint,
        EmbeddedPlayerControlsOverlayFingerprint,
        EmbeddedPlayerFingerprint,
        APIPlayerServiceFingerprint,
    ),
)
