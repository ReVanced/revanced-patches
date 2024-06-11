package app.revanced.patches.shared

import app.revanced.patcher.fingerprint.methodFingerprint

internal val castContextFetchFingerprint = methodFingerprint {
    strings("Error fetching CastContext.")
}

internal val castDynamiteModuleFingerprint = methodFingerprint {
    strings("com.google.android.gms.cast.framework.internal.CastDynamiteModuleImpl")
}

internal val castDynamiteModuleV2Fingerprint = methodFingerprint {
    strings("Failed to load module via V2: ")
}

internal val primeMethodFingerprint = methodFingerprint {
    strings("com.google.android.GoogleCamera", "com.android.vending")
}
