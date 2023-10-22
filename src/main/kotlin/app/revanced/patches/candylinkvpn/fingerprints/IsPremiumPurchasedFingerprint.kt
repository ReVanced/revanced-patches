package app.revanced.patches.candylinkvpn.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object IsPremiumPurchasedFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("PreferenceProvider;") &&
                methodDef.name == "isPremiumPurchased"
    }
)