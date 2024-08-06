package app.revanced.patches.reddit.customclients.boostforreddit.ads.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object AdmobMediationFingerprint : MethodFingerprint(
    strings = listOf("AdmobMediation: Attempting to initialize SDK")
)
