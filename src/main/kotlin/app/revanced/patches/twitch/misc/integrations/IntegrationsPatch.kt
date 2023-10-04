package app.revanced.patches.twitch.misc.integrations

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.integrations.AbstractIntegrationsPatch
import app.revanced.patches.twitch.misc.integrations.fingerprints.InitFingerprint

@Patch(requiresIntegrations = true)
object IntegrationsPatch : AbstractIntegrationsPatch(
    "Lapp/revanced/twitch/utils/ReVancedUtils;",
    setOf(InitFingerprint)
)