package app.revanced.patches.nyx.misc.pro.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object CheckProFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("BillingManager;") && methodDef.name == "isProVersion"
    }
)
