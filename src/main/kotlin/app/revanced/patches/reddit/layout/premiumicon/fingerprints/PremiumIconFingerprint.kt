package app.revanced.patches.reddit.layout.premiumicon.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object PremiumIconFingerprint : MethodFingerprint(
    returnType = "Z",
    customFingerprint = { methodDef, classDef ->
        methodDef.definingClass.endsWith("/MyAccount;") && methodDef.name == "isPremiumSubscriber" && classDef.sourceFile == "MyAccount.kt"
    }
)