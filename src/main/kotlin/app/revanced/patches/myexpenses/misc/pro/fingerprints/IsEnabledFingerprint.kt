package app.revanced.patches.myexpenses.misc.pro.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val isEnabledFingerprint = methodFingerprint {
    returns("Z")
    strings("feature", "feature.licenceStatus")
}