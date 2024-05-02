package app.revanced.patches.twitter.misc.links.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val sanitizeSharingLinksFingerprint = methodFingerprint {
    returns("Ljava/lang/String;")
    strings("<this>", "shareParam", "sessionToken")
}
