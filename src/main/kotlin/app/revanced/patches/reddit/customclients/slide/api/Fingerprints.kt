package app.revanced.patches.reddit.customclients.slide.api

import app.revanced.patcher.fingerprint.methodFingerprint

internal val getClientIdFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        if (!classDef.endsWith("Credentials;")) return@custom false

        methodDef.name == "getClientId"
    }
}
