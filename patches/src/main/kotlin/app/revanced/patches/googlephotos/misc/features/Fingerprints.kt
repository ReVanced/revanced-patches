package app.revanced.patches.googlephotos.misc.features

import app.revanced.patcher.fingerprint

internal val initializeFeaturesEnumFingerprint by fingerprint {
    strings("com.google.android.apps.photos.NEXUS_PRELOAD")
}
