package app.revanced.patches.reddit.customclients.boostforreddit.ads

import app.revanced.patcher.fingerprint

internal val maxMediationFingerprint = fingerprint {
    strings("MaxMediation: Attempting to initialize SDK")
}

internal val admobMediationFingerprint = fingerprint {
    strings("AdmobMediation: Attempting to initialize SDK")
}
