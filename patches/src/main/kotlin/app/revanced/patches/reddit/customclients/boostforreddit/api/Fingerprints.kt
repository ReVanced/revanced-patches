package app.revanced.patches.reddit.customclients.boostforreddit.api

import app.revanced.patcher.fingerprint

internal val buildUserAgentFingerprint = fingerprint {
    strings("%s:%s:%s (by /u/%s)")
}

internal val getClientIdFingerprint = fingerprint {
    custom { method, classDef ->
        if (!classDef.endsWith("Credentials;")) return@custom false

        method.name == "getClientId"
    }
}

internal val loginActivityOnCreateFingerprint = fingerprint {
    strings("http://rubenmayayo.com")
    custom { method, classDef ->
        if (!classDef.endsWith("LoginActivity;")) return@custom false

        method.name == "onCreate"
    }
}

internal val loginActivityAShouldOverrideUrlLoadingFingerprint = fingerprint {
    strings("http://rubenmayayo.com")
    custom { method, classDef ->
        if (!classDef.endsWith("LoginActivity${'$'}a;")) return@custom false

        method.name == "shouldOverrideUrlLoading"
    }
}