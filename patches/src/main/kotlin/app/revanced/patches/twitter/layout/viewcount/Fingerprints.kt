package app.revanced.patches.twitter.layout.viewcount

import app.revanced.patcher.fingerprint

internal val viewCountsEnabledFingerprint by fingerprint {
    returns("Z")
    strings("view_counts_public_visibility_enabled")
}
