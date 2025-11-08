package app.revanced.patches.myexpenses.misc.pro

import app.revanced.patcher.fingerprint

internal val isEnabledFingerprint = fingerprint {
    returns("Z")
    strings("feature", "feature.licenceStatus")
}