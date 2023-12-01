package app.revanced.patches.twitter.misc.links.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

// Adds telemetry to the share links
object AddTrackingQueryToLinkFingerprint : MethodFingerprint(
    strings = listOf("<this>", "shareParam", "sessionToken"),
)