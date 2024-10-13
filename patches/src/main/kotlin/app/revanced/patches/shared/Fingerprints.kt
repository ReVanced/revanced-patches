package app.revanced.patches.shared

import app.revanced.patcher.fingerprint

internal val castContextFetchFingerprint = fingerprint {
    strings("Error fetching CastContext.")
}

internal val castDynamiteModuleV2Fingerprint = fingerprint {
    strings("Failed to load module via V2: ")
}

internal val primeMethodFingerprint = fingerprint {
    strings("com.google.android.GoogleCamera", "com.android.vending")
}
