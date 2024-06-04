package app.revanced.patches.nyx.misc.pro.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object CheckProFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, classDef ->
        classDef.endsWith("BillingManager;") && methodDef.name == "isProVersion"
    }
)
