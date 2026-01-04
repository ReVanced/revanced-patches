package app.revanced.patches.strava.subscription

import app.revanced.patcher.fingerprint

internal val getSubscribedFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "getSubscribed" && classDef.endsWith("/SubscriptionDetailResponse;")
    }
}
