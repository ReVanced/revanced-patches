package app.revanced.patches.strava.subscription

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getSubscribedMethod by gettingFirstMutableMethodDeclaratively {
    name("getSubscribed")
    definingClass("/SubscriptionDetailResponse;"::endsWith)
}
