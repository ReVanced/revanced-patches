package app.revanced.patches.reddit.ad.comments

import app.revanced.patcher.fingerprint

internal val hideCommentAdsFingerprint = fingerprint {
    strings(
        "link",
        // CommentPageRepository is not returning a link object
        "is not returning a link object"
    )
    custom { _, classDef ->
        classDef.sourceFile == "PostDetailPresenter.kt"
    }
}