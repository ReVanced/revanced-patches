package app.revanced.patches.reddit.customclients.slide.api.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val getClientIdFingerprint = methodFingerprint {
    custom custom@{ methodDef, classDef ->
        if (!classDef.endsWith("Credentials;")) return@custom false

        methodDef.name == "getClientId"
    }
}