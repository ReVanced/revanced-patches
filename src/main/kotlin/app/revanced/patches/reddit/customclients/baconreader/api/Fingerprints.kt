package app.revanced.patches.reddit.customclients.baconreader.api

import app.revanced.patcher.fingerprint.methodFingerprint

internal val getAuthorizationUrlFingerprint = methodFingerprint {
    strings("client_id=zACVn0dSFGdWqQ")
}
internal val getClientIdFingerprint = methodFingerprint {
    strings("client_id=zACVn0dSFGdWqQ")
    custom { methodDef, classDef ->
        if (!classDef.endsWith("RedditOAuth;")) return@custom false

        methodDef.name == "getAuthorizeUrl"
    }
}

internal val requestTokenFingerprint = methodFingerprint {
    strings("zACVn0dSFGdWqQ", "kDm2tYpu9DqyWFFyPlNcXGEni4k") // App ID and secret.
}