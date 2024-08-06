package app.revanced.patches.reddit.customclients.boostforreddit.ads.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object MaxMediationFingerprint : MethodFingerprint(
    strings = listOf("MaxMediation: Attempting to initialize SDK")
)
