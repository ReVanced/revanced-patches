package app.revanced.patches.reddit.customclients.boostforreddit.api

import app.revanced.patcher.fingerprint.methodFingerprint

internal val buildUserAgentFingerprint = methodFingerprint {
    strings("%s:%s:%s (by /u/%s)")
}

internal val getClientIdFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        if (!classDef.endsWith("Credentials;")) return@custom false

        methodDef.name == "getClientId"
    }
}
