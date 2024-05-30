package app.revanced.patches.shared.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val castContextFetchFingerprint = methodFingerprint {
    strings("Error fetching CastContext.")
}
