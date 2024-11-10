package app.revanced.patches.nyx.misc.pro

import app.revanced.patcher.fingerprint

internal val checkProFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("BillingManager;") && method.name == "isProVersion"
    }
}
