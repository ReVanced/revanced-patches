package app.revanced.patches.reddit.ad.comments.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val hideCommentAdsFingerprint = methodFingerprint {
    strings(
        "link",
        // CommentPageRepository is not returning a link object
        "is not returning a link object"
    )
    custom { _, classDef ->
        classDef.sourceFile == "PostDetailPresenter.kt"
    }
}