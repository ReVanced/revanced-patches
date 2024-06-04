package app.revanced.patches.shared.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val primeMethodFingerprint = methodFingerprint {
    strings("com.google.android.GoogleCamera", "com.android.vending")
}
