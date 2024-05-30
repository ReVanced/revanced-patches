package app.revanced.patches.reddit.customclients.boostforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object JRAWUserAgent : MethodFingerprint(
    strings = listOf("platform", "appId", "version", "redditUsername"),
)