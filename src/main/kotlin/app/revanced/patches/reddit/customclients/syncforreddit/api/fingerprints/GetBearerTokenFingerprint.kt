package app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object GetBearerTokenFingerprint : MethodFingerprint(
    strings = listOf("Basic")
)