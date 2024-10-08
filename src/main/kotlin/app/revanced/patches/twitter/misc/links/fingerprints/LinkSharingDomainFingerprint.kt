package app.revanced.patches.twitter.misc.links.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object LinkSharingDomainFingerprint : MethodFingerprint(
    strings = listOf("https://fxtwitter.com"),
)