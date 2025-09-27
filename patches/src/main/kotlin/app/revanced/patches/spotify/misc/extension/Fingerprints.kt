package app.revanced.patches.spotify.misc.extension

import app.revanced.patcher.fingerprint

internal val loadOrbitLibraryFingerprint by fingerprint {
    strings("OrbitLibraryLoader", "cst")
}
