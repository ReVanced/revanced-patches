package app.revanced.patches.reddit.customclients.boostforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val buildUserAgentFingerprint = methodFingerprint {
    strings("%s:%s:%s (by /u/%s)")
}
