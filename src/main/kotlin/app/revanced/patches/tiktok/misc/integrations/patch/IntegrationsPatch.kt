package app.revanced.patches.tiktok.misc.integrations.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patches.tiktok.misc.integrations.annotations.IntegrationsCompatibility
import app.revanced.patches.tiktok.misc.integrations.fingerprints.InitFingerprint
import app.revanced.patches.shared.integrations.patch.AbstractIntegrationsPatch

@Name("integrations")
@IntegrationsCompatibility
class IntegrationsPatch : AbstractIntegrationsPatch(
    "Lapp/revanced/tiktok/utils/ReVancedUtils;",
    listOf(InitFingerprint)
)