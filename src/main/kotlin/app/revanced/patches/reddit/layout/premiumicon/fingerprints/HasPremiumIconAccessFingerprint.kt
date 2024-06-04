package app.revanced.patches.reddit.layout.premiumicon.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object HasPremiumIconAccessFingerprint : MethodFingerprint(
    "Z",
    customFingerprint = { methodDef, classDef ->
        classDef.endsWith("MyAccount;") && methodDef.name == "isPremiumSubscriber"
    }
)