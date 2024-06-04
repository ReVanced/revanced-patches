package app.revanced.patches.candylinkvpn.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object IsPremiumPurchasedFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, classDef ->
        classDef.endsWith("PreferenceProvider;") &&
                methodDef.name == "isPremiumPurchased"
    }
)