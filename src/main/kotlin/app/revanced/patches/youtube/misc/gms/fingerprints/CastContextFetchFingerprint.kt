package app.revanced.patches.youtube.misc.gms.fingerprints


import app.revanced.patcher.fingerprint.MethodFingerprint

internal object CastContextFetchFingerprint : MethodFingerprint(
    strings = listOf("Error fetching CastContext.")
)