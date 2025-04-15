package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.fingerprint

internal val getPackageInfoFingerprint = fingerprint {
    strings(
        "Failed to get the application signatures",
        "Failed to get installer package"
    )
}

internal val getPackageInfoLegacyFingerprint = fingerprint {
    strings(
        "Failed to get the application signatures"
    )
}

