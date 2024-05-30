package app.revanced.patches.reddit.customclients.joeyforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object AuthUtilityUserAgent : MethodFingerprint (
    strings = setOf("2.1.6.5"),
    returnType = "Ljava/lang/String;",
    customFingerprint = { _, classDef ->
        classDef.sourceFile == "AuthUtility.java"
    },
)