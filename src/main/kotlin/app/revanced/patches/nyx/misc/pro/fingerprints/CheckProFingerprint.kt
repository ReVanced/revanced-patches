package app.revanced.patches.nyx.misc.pro.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val checkProFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("BillingManager;") && methodDef.name == "isProVersion"
    }
}