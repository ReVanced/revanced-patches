package app.revanced.patches.candylinkvpn.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val isPremiumPurchasedFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("PreferenceProvider;") &&
                methodDef.name == "isPremiumPurchased"
    }
}