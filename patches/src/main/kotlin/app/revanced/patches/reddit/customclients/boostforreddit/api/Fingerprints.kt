package app.revanced.patches.reddit.customclients.boostforreddit.api

import app.revanced.patcher.fingerprint

internal val buildUserAgentFingerprint by fingerprint {
    strings("%s:%s:%s (by /u/%s)")
}

internal val getClientIdFingerprint by fingerprint {
    custom { method, classDef ->
        if (!classDef.endsWith("Credentials;")) return@custom false

        method.name == "getClientId"
    }
}
