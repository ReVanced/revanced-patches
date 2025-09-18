package app.revanced.patches.instagram.feed

import app.revanced.patcher.fingerprint

internal val mainFeedRequestClassFingerprint by fingerprint {
    strings("Request{mReason=", ", mInstanceNumber=")
}

internal val mainFeedHeaderMapFinderFingerprint by fingerprint {
    strings("pagination_source", "FEED_REQUEST_SENT")
}
