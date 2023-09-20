package app.revanced.patches.youtube.misc.integrations

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.integrations.AbstractIntegrationsPatch
import app.revanced.patches.youtube.misc.integrations.fingerprints.*

@Patch(requiresIntegrations = true)
object IntegrationsPatch : AbstractIntegrationsPatch(
    "Lapp/revanced/integrations/utils/ReVancedUtils;",
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