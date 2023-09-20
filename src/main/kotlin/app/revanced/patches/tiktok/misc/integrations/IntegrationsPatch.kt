package app.revanced.patches.tiktok.misc.integrations

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.integrations.AbstractIntegrationsPatch
import app.revanced.patches.tiktok.misc.integrations.fingerprints.InitFingerprint

@Patch(requiresIntegrations = true)
object IntegrationsPatch : AbstractIntegrationsPatch(
    "Lapp/revanced/tiktok/utils/ReVancedUtils;",
    setOf(InitFingerprint)
)