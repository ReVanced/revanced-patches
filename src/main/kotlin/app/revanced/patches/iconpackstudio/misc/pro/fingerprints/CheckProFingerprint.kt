package app.revanced.patches.iconpackstudio.misc.pro.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object CheckProFingerprint : MethodFingerprint(
    "Z",
    customFingerprint = { methodDef, _ -> methodDef.definingClass.endsWith("IPSPurchaseRepository;")}
)
