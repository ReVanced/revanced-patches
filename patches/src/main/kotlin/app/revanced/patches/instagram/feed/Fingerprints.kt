package app.revanced.patches.instagram.feed

import app.revanced.patcher.fingerprint

internal val mainFeedRequestClassFingerprint = fingerprint {
    strings("Request{mReason=", ", mInstanceNumber=")
}

internal val mainFeedHeaderMapFinderFingerprint = fingerprint {
    strings("pagination_source", "FEED_REQUEST_SENT")
}
