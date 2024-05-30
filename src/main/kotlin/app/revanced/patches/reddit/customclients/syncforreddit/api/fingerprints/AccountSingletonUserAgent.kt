package app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object AccountSingletonUserAgent : MethodFingerprint(
    strings = listOf("android:com.laurencedawson.reddit_sync:vv23.06.30-13:39 (by /u/ljdawson)"),
)