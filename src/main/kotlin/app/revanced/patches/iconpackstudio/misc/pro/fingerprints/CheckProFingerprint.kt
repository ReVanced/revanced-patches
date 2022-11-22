package app.revanced.patches.iconpackstudio.misc.pro.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object CheckProFingerprint : MethodFingerprint(
    "Z",
    customFingerprint = { it.definingClass.endsWith("IPSPurchaseRepository;")}
)
