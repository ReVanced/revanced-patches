package app.revanced.patches.shared

import app.revanced.patcher.fingerprint
import app.revanced.patcher.addString

internal val castContextFetchFingerprint = fingerprint {
    instructions(
        addString("Error fetching CastContext.")
    )
}

internal val primeMethodFingerprint = fingerprint {
    instructions(
        addString("com.android.vending"),
        addString("com.google.android.GoogleCamera")
    )
}
