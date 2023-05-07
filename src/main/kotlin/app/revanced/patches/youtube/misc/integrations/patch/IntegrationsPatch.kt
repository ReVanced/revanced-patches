package app.revanced.patches.youtube.misc.integrations.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.patch.annotations.RequiresIntegrations
import app.revanced.patches.shared.integrations.patch.AbstractIntegrationsPatch
import app.revanced.patches.youtube.misc.integrations.annotations.IntegrationsCompatibility
import app.revanced.patches.youtube.misc.integrations.fingerprints.InitFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.EmbeddedPlayerControlsOverlayFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.ServiceFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.StandalonePlayerFingerprint

@Name("integrations")
@IntegrationsCompatibility
@RequiresIntegrations
class IntegrationsPatch : AbstractIntegrationsPatch(
    "Lapp/revanced/integrations/utils/ReVancedUtils;",
    listOf(InitFingerprint, StandalonePlayerFingerprint, ServiceFingerprint, EmbeddedPlayerControlsOverlayFingerprint),
)