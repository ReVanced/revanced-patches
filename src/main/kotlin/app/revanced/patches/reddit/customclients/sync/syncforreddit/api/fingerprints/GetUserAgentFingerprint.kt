package app.revanced.patches.reddit.customclients.sync.syncforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val getUserAgentFingerprint = methodFingerprint {
    strings("android:com.laurencedawson.reddit_sync")
}
