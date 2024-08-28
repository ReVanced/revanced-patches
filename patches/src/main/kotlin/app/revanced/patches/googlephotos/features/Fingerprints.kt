package app.revanced.patches.googlephotos.features

import app.revanced.patcher.fingerprint

internal val initializeFeaturesEnumFingerprint = fingerprint {
    strings("com.google.android.apps.photos.NEXUS_PRELOAD")
}
