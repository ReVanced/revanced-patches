package app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object GetBearerTokenFingerprint : MethodFingerprint(
    strings = listOf("Basic")
)