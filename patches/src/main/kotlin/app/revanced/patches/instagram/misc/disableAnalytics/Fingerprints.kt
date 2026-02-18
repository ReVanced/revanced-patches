package app.revanced.patches.instagram.misc.disableAnalytics

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.strings

internal val BytecodePatchContext.instagramAnalyticsUrlBuilderMethod by gettingFirstMethodDeclaratively {
    strings("/logging_client_events")
}

internal const val TARGET_URL = "https://graph.facebook.com/logging_client_events"
internal val BytecodePatchContext.facebookAnalyticsUrlInitMethod by gettingFirstMethodDeclaratively {
    strings("analytics_endpoint", TARGET_URL)
}
