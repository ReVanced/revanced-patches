package app.revanced.patches.reddit.customclients.baconreader.api.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object RequestTokenFingerprint : MethodFingerprint(
    strings = listOf("zACVn0dSFGdWqQ", "kDm2tYpu9DqyWFFyPlNcXGEni4k"), // App ID and secret.
)