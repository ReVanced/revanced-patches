package app.revanced.patches.music.misc.integrations

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.misc.integrations.fingerprints.ApplicationInitFingerprint
import app.revanced.patches.shared.integrations.AbstractIntegrationsPatch

@Patch(requiresIntegrations = true)
object IntegrationsPatch : AbstractIntegrationsPatch(
    "Lapp/revanced/integrations/utils/ReVancedUtils;",
    setOf(
        ApplicationInitFingerprint,
    ),
)
