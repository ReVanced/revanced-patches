package app.revanced.patches.twitter.misc.links.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object AddTrackingQueryToLinkFingerprint : MethodFingerprint(
    strings = listOf("<this>", "shareParam", "sessionToken"),
)