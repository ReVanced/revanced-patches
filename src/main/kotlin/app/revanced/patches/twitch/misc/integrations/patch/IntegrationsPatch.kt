package app.revanced.patches.twitch.misc.integrations.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patches.twitch.misc.integrations.fingerprints.InitFingerprint
import app.revanced.patches.twitch.misc.integrations.annotations.IntegrationsCompatibility
import app.revanced.shared.patches.AbstractIntegrationsPatch

@Name("integrations")
@IntegrationsCompatibility
class IntegrationsPatch : AbstractIntegrationsPatch(
    "Lapp/revanced/twitch/utils/ReVancedUtils;",
    listOf(InitFingerprint)
)