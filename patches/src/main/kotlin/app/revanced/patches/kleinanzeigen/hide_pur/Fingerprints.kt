package app.revanced.patches.kleinanzeigen.hide_pur

import app.revanced.patcher.gettingFirstMethod
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getShowAdFreeSubscriptionFingerprint by gettingFirstMethod {
    name == "getShowAdFreeSubscription"
}
