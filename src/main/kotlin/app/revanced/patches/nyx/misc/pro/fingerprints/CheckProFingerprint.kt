package app.revanced.patches.nyx.misc.pro.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object CheckProFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("BillingManager;") && methodDef.name == "isProVersion"
    }
)
