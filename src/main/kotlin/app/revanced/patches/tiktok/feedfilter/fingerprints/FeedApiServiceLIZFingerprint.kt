package app.revanced.patches.tiktok.feedfilter.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object FeedApiServiceLIZFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/FeedApiService;") && methodDef.name == "fetchFeedList"
    }
)