package app.revanced.patches.reddit.customclients.slide.api

import app.revanced.patcher.fingerprint

internal val getClientIdFingerprint = fingerprint {
    custom { method, classDef ->
        if (!classDef.endsWith("Credentials;")) return@custom false

        method.name == "getClientId"
    }
}
