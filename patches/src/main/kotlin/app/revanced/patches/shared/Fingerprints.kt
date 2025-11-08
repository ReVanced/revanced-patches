package app.revanced.patches.shared

import app.revanced.patcher.fingerprint
import app.revanced.patcher.string

internal val castContextFetchFingerprint = fingerprint {
    instructions(
        string("Error fetching CastContext.")
    )
}

internal val primeMethodFingerprint = fingerprint {
    instructions(
        string("com.android.vending"),
        string("com.google.android.GoogleCamera")
    )
}
