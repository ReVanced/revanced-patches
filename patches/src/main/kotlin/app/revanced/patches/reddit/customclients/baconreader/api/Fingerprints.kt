package app.revanced.patches.reddit.customclients.baconreader.api

import app.revanced.patcher.fingerprint

internal val getAuthorizationUrlFingerprint by fingerprint {
    strings("client_id=zACVn0dSFGdWqQ")
}
internal val getClientIdFingerprint by fingerprint {
    strings("client_id=zACVn0dSFGdWqQ")
    custom { method, classDef ->
        if (!classDef.endsWith("RedditOAuth;")) return@custom false

        method.name == "getAuthorizeUrl"
    }
}

internal val requestTokenFingerprint by fingerprint {
    strings("zACVn0dSFGdWqQ", "kDm2tYpu9DqyWFFyPlNcXGEni4k") // App ID and secret.
}
