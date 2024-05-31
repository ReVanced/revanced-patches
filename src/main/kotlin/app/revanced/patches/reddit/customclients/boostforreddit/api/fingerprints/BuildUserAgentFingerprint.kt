package app.revanced.patches.reddit.customclients.boostforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object BuildUserAgentFingerprint : MethodFingerprint(
    strings = listOf("%s:%s:%s (by /u/%s)"),
)
