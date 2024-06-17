package app.revanced.patches.candylinkvpn

import app.revanced.patcher.fingerprint

internal val isPremiumPurchasedFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("PreferenceProvider;") &&
            method.name == "isPremiumPurchased"
    }
}
