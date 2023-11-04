package app.revanced.patches.youtube.misc.microg.fingerprints


import app.revanced.patcher.fingerprint.MethodFingerprint

object CastContextFetchFingerprint : MethodFingerprint(
    strings = listOf("Error fetching CastContext.")
)