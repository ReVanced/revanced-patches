package app.revanced.patches.kleinanzeigen.hide_pur

import app.revanced.patcher.fingerprint

internal val getShowAdFreeSubscriptionFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "getShowAdFreeSubscription"
    }
}
