package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.fingerprint
import app.revanced.patcher.string

internal val getPackageInfoFingerprint by fingerprint {
    instructions(
        string("Failed to get the application signatures")
    )
}
