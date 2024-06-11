package app.revanced.patches.tiktok.feedfilter

import app.revanced.patcher.fingerprint.methodFingerprint

internal val feedApiServiceLIZFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("/FeedApiService;") && methodDef.name == "fetchFeedList"
    }
}
