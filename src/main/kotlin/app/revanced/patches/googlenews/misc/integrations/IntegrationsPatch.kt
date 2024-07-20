package app.revanced.patches.googlenews.misc.integrations

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.googlenews.misc.integrations.fingerprints.StartActivityInitFingerprint
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch

@Patch(requiresIntegrations = true)
object IntegrationsPatch : BaseIntegrationsPatch(
    setOf(StartActivityInitFingerprint),
)
