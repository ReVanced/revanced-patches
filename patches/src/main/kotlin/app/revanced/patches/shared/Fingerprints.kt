package app.revanced.patches.shared

import app.revanced.patcher.fingerprint

internal val castContextFetchFingerprint by fingerprint {
    strings("Error fetching CastContext.")
}

internal val primeMethodFingerprint by fingerprint {
    strings("com.google.android.GoogleCamera", "com.android.vending")
}
