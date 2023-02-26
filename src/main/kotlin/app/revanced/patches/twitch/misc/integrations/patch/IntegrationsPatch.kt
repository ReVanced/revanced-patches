package app.revanced.patches.twitch.misc.integrations.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.patch.annotations.RequiresIntegrations
import app.revanced.patches.shared.integrations.patch.AbstractIntegrationsPatch
import app.revanced.patches.twitch.misc.integrations.annotations.IntegrationsCompatibility
import app.revanced.patches.twitch.misc.integrations.fingerprints.InitFingerprint

@Name("integrations")
@IntegrationsCompatibility
@RequiresIntegrations
class IntegrationsPatch : AbstractIntegrationsPatch(
    "Lapp/revanced/twitch/utils/ReVancedUtils;",
    listOf(InitFingerprint)
)