package app.revanced.patches.reddit.customclients.syncforreddit.fix.video.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object RedditVideoRequestFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, classDef ->
        classDef.sourceFile == "RedditVideoRequest.java" && methodDef.name == "parseNetworkResponse"
    }
)
