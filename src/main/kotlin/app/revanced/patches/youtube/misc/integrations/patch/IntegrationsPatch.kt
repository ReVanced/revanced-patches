package app.revanced.patches.youtube.misc.integrations.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.patch.annotations.RequiresIntegrations
import app.revanced.patches.shared.integrations.patch.AbstractIntegrationsPatch
import app.revanced.patches.youtube.misc.integrations.annotations.IntegrationsCompatibility
import app.revanced.patches.youtube.misc.integrations.fingerprints.EmbeddedPlayerControlsOverlayFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.EmbeddedPlayerFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.ApplicationInitFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.RemoteEmbedFragmentFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.RemoteEmbeddedPlayerFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.APIPlayerServiceFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.StandalonePlayerActivityFingerprint

@Name("integrations")
@IntegrationsCompatibility
@RequiresIntegrations
class IntegrationsPatch : AbstractIntegrationsPatch(
    "Lapp/revanced/integrations/utils/ReVancedUtils;",
    listOf(
        ApplicationInitFingerprint,
        StandalonePlayerActivityFingerprint,
        RemoteEmbeddedPlayerFingerprint,
        RemoteEmbedFragmentFingerprint,
        EmbeddedPlayerControlsOverlayFingerprint,
        EmbeddedPlayerFingerprint,
        APIPlayerServiceFingerprint,
    ),
)