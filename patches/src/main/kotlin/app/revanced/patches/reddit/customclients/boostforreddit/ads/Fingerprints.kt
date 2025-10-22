package app.revanced.patches.reddit.customclients.boostforreddit.ads

import app.revanced.patcher.fingerprint

internal val maxMediationFingerprint by fingerprint {
    strings("MaxMediation: Attempting to initialize SDK")
}

internal val admobMediationFingerprint by fingerprint {
    strings("AdmobMediation: Attempting to initialize SDK")
}
