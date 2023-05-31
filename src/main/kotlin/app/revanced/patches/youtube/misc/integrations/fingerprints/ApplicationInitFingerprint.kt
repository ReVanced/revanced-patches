package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.patches.shared.integrations.patch.AbstractIntegrationsPatch.IntegrationsFingerprint

/**
 * Hooks the context when the app is launched as a regular application (and is not an embedded video playback).
 */
object ApplicationInitFingerprint : IntegrationsFingerprint(
    strings = listOf("Application creation", "Application.onCreate"),
    // Integrations context is the Activity itself.
)