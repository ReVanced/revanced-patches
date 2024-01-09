package app.revanced.patches.shared.fingerprints


import app.revanced.patcher.fingerprint.MethodFingerprint

internal object CastContextFetchFingerprint : MethodFingerprint(
    strings = listOf("Error fetching CastContext.")
)