package app.revanced.patches.music.misc.integrations

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.misc.integrations.fingerprints.ApplicationInitFingerprint
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch

@Patch(requiresIntegrations = true)
object IntegrationsPatch : BaseIntegrationsPatch(
    setOf(ApplicationInitFingerprint),
)
