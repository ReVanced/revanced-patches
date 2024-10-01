package app.revanced.patches.youtube.misc.integrations

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch
import app.revanced.patches.youtube.misc.integrations.fingerprints.ApplicationInitFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.RemoteEmbedFragmentFingerprint
import app.revanced.patches.youtube.misc.integrations.fingerprints.RemoteEmbeddedPlayerFingerprint

@Patch(requiresIntegrations = true)
object IntegrationsPatch : BaseIntegrationsPatch(
    setOf(
        ApplicationInitFingerprint,
        RemoteEmbeddedPlayerFingerprint,
        RemoteEmbedFragmentFingerprint,
    ),
)
