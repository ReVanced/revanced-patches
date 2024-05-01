package app.revanced.patches.iconpackstudio.misc.pro.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val checkProFingerprint = methodFingerprint {
    returns("Z")
    custom { methodDef, _ -> methodDef.definingClass.endsWith("IPSPurchaseRepository;") }
}
