package app.revanced.patches.reddit.customclients.baconreader.api.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object GetAuthorizationUrlFingerprint: MethodFingerprint(
    strings = listOf("client_id=zACVn0dSFGdWqQ"),
)