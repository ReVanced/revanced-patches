package app.revanced.patches.reddit.ad.comments.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object HideCommentAdsFingerprint : MethodFingerprint(
    strings = listOf(
        "link",
        // CommentPageRepository is not returning a link object
        "is not returning a link object"
    ),
    customFingerprint = { _, classDef ->
        classDef.sourceFile == "PostDetailPresenter.kt"
    },
)
