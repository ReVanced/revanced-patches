package app.revanced.patches.reddit.utils.integrations

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.utils.integrations.fingerprints.InitFingerprint
import app.revanced.patches.shared.patch.integrations.AbstractIntegrationsPatch

@Patch(requiresIntegrations = true)
object IntegrationsPatch : AbstractIntegrationsPatch(
    "Lapp/revanced/integrations/reddit/utils/ReVancedUtils;",
    setOf(InitFingerprint),
)