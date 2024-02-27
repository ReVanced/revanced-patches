package app.revanced.patches.music.utils.microg.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object CastContextFetchFingerprint : MethodFingerprint(
    strings = listOf("Error fetching CastContext.")
)