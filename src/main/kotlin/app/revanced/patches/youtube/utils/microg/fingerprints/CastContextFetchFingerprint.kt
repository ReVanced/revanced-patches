package app.revanced.patches.youtube.utils.microg.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object CastContextFetchFingerprint : MethodFingerprint(
    strings = listOf("Error fetching CastContext.")
)