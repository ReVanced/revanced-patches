package app.revanced.patches.reddit.layout.premiumicon.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object HasPremiumIconAccessFingerprint : MethodFingerprint(
    "Z",
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("MyAccount;") && methodDef.name == "isPremiumSubscriber"
    }
)