package app.revanced.patches.tiktok.misc.integrations

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.integrations.AbstractIntegrationsPatch
import app.revanced.patches.tiktok.misc.integrations.fingerprints.InitFingerprint

@Patch(requiresIntegrations = true)
object IntegrationsPatch : AbstractIntegrationsPatch(
    setOf(InitFingerprint)
)