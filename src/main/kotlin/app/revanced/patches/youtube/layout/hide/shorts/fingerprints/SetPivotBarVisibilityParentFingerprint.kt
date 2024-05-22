package app.revanced.patches.youtube.layout.hide.shorts.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val setPivotBarVisibilityParentFingerprint = methodFingerprint {
    parameters("Z")
    strings("FEnotifications_inbox")
}
