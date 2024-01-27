package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch.IntegrationsFingerprint

/**
 * Hooks the context when the app is launched as a regular application (and is not an embedded video playback).
 */
internal object ApplicationInitFingerprint : IntegrationsFingerprint(
    strings = listOf("Application creation", "Application.onCreate"),
    // Integrations context is the Activity itself.
)