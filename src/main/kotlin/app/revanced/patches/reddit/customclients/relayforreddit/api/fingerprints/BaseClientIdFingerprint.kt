package app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal fun baseClientIdFingerprint(string: String) = methodFingerprint {
    strings("dj-xCIZQYiLbEg", string)
}
