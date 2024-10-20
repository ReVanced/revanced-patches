package app.revanced.patches.youtube.misc.integrations

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch
import app.revanced.patches.youtube.misc.integrations.fingerprints.APIPlayerServiceFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.ApplicationInitFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.EmbeddedPlayerControlsOverlayFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.EmbeddedPlayerFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.RemoteEmbedFragmentFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.RemoteEmbeddedPlayerFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.StandalonePlayerActivityFingerprint

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
