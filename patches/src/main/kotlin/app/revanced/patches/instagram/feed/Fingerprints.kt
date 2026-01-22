package app.revanced.patches.instagram.feed

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.mainFeedRequestClassMethod by gettingFirstMutableMethodDeclaratively(
    "Request{mReason=", ", mInstanceNumber="
)

internal val BytecodePatchContext.mainFeedHeaderMapFinderMethod by gettingFirstMutableMethodDeclaratively(
    "pagination_source", "FEED_REQUEST_SENT"
)
