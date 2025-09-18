package app.revanced.patches.instagram.feed

import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.BytecodePatchContext

internal val mainFeedRequestClassFingerprint = fingerprint {
    strings("Request{mReason=", ", mInstanceNumber=")
}

context(BytecodePatchContext)
internal val initMainFeedRequestFingerprint get() = fingerprint {
    custom { method, classDef ->
        method.name == "<init>" &&
                classDef == mainFeedRequestClassFingerprint.classDef
    }
}

internal val mainFeedHeaderMapFinderFingerprint = fingerprint {
    strings("pagination_source", "FEED_REQUEST_SENT")
}
