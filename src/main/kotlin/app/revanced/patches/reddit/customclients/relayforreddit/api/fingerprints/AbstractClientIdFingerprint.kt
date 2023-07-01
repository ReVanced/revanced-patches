package app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

abstract class AbstractClientIdFingerprint(string: String) : MethodFingerprint(
    strings = listOfNotNull("dj-xCIZQYiLbEg", string),
)