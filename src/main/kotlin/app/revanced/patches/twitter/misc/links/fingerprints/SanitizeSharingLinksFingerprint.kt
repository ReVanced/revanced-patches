package app.revanced.patches.twitter.misc.links.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object SanitizeSharingLinksFingerprint : MethodFingerprint(
    strings = listOf("<this>", "shareParam", "sessionToken"),
    returnType = "Ljava/lang/String;",
)