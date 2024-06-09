package app.revanced.patches.reddit.layout.premiumicon.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val hasPremiumIconAccessFingerprint = methodFingerprint {
    returns("Z")
    custom { methodDef, classDef ->
        classDef.endsWith("MyAccount;") && methodDef.name == "isPremiumSubscriber"
    }
}