package app.revanced.patches.spotify.misc.extension

import app.revanced.patcher.fingerprint

internal val loadOrbitLibraryFingerprint = fingerprint {
    strings("orbit_library_load", "orbit-jni-spotify")
}
