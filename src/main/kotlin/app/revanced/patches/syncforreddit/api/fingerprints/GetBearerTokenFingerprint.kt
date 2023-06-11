package app.revanced.patches.syncforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object GetBearerTokenFingerprint : MethodFingerprint(
    strings = listOf("Basic")
)