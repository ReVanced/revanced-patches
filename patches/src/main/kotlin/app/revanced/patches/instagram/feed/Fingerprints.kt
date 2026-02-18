package app.revanced.patches.instagram.feed

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.mainFeedRequestClassMethod by gettingFirstMethodDeclaratively(
    "Request{mReason=", ", mInstanceNumber="
)

internal val BytecodePatchContext.mainFeedHeaderMapFinderMethod by gettingFirstMethodDeclaratively(
    "pagination_source", "FEED_REQUEST_SENT"
)
