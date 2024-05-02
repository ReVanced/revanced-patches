package app.revanced.patches.twitter.layout.viewcount.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val viewCountsEnabledFingerprint = methodFingerprint {
    returns("Z")
    strings("view_counts_public_visibility_enabled")
}
