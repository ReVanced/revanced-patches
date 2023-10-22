package app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object GetBearerTokenFingerprint : MethodFingerprint(
    strings = listOf("Basic")
)