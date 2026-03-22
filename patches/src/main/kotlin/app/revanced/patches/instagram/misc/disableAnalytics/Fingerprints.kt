package app.revanced.patches.instagram.misc.disableAnalytics

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.strings
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.strings

internal val BytecodePatchContext.instagramAnalyticsUrlBuilderMethod by gettingFirstMethodDeclaratively {
    strings("/logging_client_events")
}

internal val BytecodePatchContext.facebookAnalyticsUrlInitMethodMatch by composingFirstMethod {
    strings(
        "analytics_endpoint",
        "https://graph.facebook.com/logging_client_events"
    )
}
