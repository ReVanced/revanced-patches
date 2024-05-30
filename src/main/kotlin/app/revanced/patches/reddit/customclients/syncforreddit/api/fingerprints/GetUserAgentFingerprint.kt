package app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object GetUserAgentFingerprint : MethodFingerprint(
    strings = listOf("android:com.laurencedawson.reddit_sync"),
)
