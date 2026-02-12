package app.revanced.patches.instagram.misc.disableAnalytics

import app.revanced.patcher.fingerprint

internal val instagramAnalyticsUrlBuilderMethodFingerprint = fingerprint {
    strings("/logging_client_events")
}

internal const val TARGET_URL = "https://graph.facebook.com/logging_client_events"
internal val facebookAnalyticsUrlInitMethodFingerprint = fingerprint {
    strings("analytics_endpoint",TARGET_URL)
}
