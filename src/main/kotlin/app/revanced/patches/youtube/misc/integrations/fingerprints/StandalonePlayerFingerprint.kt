package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.shared.patches.AbstractIntegrationsPatch.IntegrationsFingerprint

object StandalonePlayerFingerprint : IntegrationsFingerprint(
    strings = listOf(
        "Invalid PlaybackStartDescriptor. Returning the instance itself.",
        "com.google.android.music",
    ),
)