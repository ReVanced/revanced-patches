package app.revanced.patches.magazines.misc.integrations

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.magazines.misc.integrations.fingerprints.StartActivityInitFingerprint
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch

@Patch(requiresIntegrations = true)
object IntegrationsPatch : BaseIntegrationsPatch(
    setOf(
        StartActivityInitFingerprint
    ),
)
