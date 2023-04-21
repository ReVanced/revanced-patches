package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.patches.shared.integrations.patch.AbstractIntegrationsPatch.IntegrationsFingerprint

object InitFingerprint : IntegrationsFingerprint(
    strings = listOf("Application creation", "Application.onCreate"),
)