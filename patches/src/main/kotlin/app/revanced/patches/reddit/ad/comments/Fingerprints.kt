package app.revanced.patches.reddit.ad.comments

import app.revanced.patcher.fingerprint

internal val hideCommentAdsFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "invokeSuspend" &&
            classDef.contains("LoadAdsCombinedCall")
    }
}
