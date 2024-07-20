package app.revanced.patches.googlephotos.misc.integrations

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.googlephotos.misc.integrations.fingerprints.HomeActivityInitFingerprint
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch

@Patch(requiresIntegrations = true)
object IntegrationsPatch : BaseIntegrationsPatch(
    setOf(HomeActivityInitFingerprint),
)
