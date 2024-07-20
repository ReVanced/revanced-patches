package app.revanced.patches.reddit.customclients.syncforreddit.misc.integrations

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch
import app.revanced.patches.reddit.customclients.syncforreddit.misc.integrations.fingerprints.InitFingerprint

@Patch(requiresIntegrations = true)
object IntegrationsPatch : BaseIntegrationsPatch(
    setOf(InitFingerprint)
)