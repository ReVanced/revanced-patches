package app.revanced.patches.reddit.layout.premiumicon

import app.revanced.patcher.fingerprint

internal val hasPremiumIconAccessFingerprint = fingerprint {
    returns("Z")
    custom { method, classDef ->
        classDef.endsWith("MyAccount;") && method.name == "isPremiumSubscriber"
    }
}
