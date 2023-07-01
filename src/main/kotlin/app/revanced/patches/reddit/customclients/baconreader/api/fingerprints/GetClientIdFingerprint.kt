package app.revanced.patches.reddit.customclients.baconreader.api.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object GetClientIdFingerprint : MethodFingerprint(
    strings = listOf("client_id=zACVn0dSFGdWqQ"),
    customFingerprint = custom@{ methodDef, classDef ->
        if (!classDef.type.endsWith("RedditOAuth;")) return@custom false

        methodDef.name == "getAuthorizeUrl"
    }
)