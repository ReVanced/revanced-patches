package app.revanced.patches.tiktok.feedfilter.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val feedApiServiceLIZFingerprint = methodFingerprint {
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/FeedApiService;") && methodDef.name == "fetchFeedList"
    }
}
