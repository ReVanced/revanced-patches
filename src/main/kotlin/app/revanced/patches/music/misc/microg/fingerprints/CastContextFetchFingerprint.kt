package app.revanced.patches.music.misc.microg.fingerprints


import app.revanced.patcher.fingerprint.MethodFingerprint

internal object CastContextFetchFingerprint : MethodFingerprint(
    strings = listOf("Error fetching CastContext.")
)