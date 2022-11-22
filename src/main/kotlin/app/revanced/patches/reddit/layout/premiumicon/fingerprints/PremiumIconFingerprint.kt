package app.revanced.patches.reddit.layout.premiumicon.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object PremiumIconFingerprint : MethodFingerprint(
    "Z",
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("MyAccount;") && methodDef.name == "isPremiumSubscriber"
    }
)