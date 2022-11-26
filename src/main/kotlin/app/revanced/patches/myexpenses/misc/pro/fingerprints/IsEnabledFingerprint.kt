package app.revanced.patches.myexpenses.misc.pro.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object IsEnabledFingerprint : MethodFingerprint(
    "Z",
    strings = listOf("feature", "feature.licenceStatus")
)
