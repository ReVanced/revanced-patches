package app.revanced.patches.instagram.misc.integrations

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.instagram.misc.integrations.fingerprints.MainActivityOnCreateFingerprint
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch

@Patch(requiresIntegrations = true)
object IntegrationsPatch : BaseIntegrationsPatch(
    setOf(MainActivityOnCreateFingerprint),
)
