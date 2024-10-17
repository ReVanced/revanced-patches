package app.revanced.patches.googlephotos.features.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object InitializeFeaturesEnumFingerprint : MethodFingerprint(
    strings = listOf("com.google.android.apps.photos.NEXUS_PRELOAD"),
)
