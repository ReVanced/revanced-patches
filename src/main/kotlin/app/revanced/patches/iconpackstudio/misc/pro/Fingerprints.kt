package app.revanced.patches.iconpackstudio.misc.pro

import app.revanced.patcher.fingerprint.methodFingerprint

internal val checkProFingerprint = methodFingerprint {
    returns("Z")
    custom { _, classDef -> classDef.endsWith("IPSPurchaseRepository;") }
}