package app.revanced.patches.reddit.customclients.baconreader.api.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val getClientIdFingerprint = methodFingerprint {
    strings("client_id=zACVn0dSFGdWqQ")
    custom custom@{ methodDef, classDef ->
        if (!classDef.endsWith("RedditOAuth;")) return@custom false

        methodDef.name == "getAuthorizeUrl"
    }
}