package app.revanced.patches.twitter.misc.links.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

// Returns a shareable link string based on a tweet ID and a username.
internal object LinkBuilderFingerprint : MethodFingerprint(
    strings = listOf("/%1\$s/status/%2\$d"),
)