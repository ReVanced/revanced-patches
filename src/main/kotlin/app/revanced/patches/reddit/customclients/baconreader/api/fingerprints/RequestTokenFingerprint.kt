package app.revanced.patches.reddit.customclients.baconreader.api.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val requestTokenFingerprint = methodFingerprint {
    strings("zACVn0dSFGdWqQ", "kDm2tYpu9DqyWFFyPlNcXGEni4k") // App ID and secret.
}