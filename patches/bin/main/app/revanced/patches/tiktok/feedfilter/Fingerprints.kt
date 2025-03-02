package app.revanced.patches.tiktok.feedfilter

import app.revanced.patcher.fingerprint

internal val feedApiServiceLIZFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/FeedApiService;") && method.name == "fetchFeedList"
    }
}
