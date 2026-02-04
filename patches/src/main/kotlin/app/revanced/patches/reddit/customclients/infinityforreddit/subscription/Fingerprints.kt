package app.revanced.patches.reddit.customclients.infinityforreddit.subscription

import app.revanced.patcher.gettingFirstMethod
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.invoke
import app.revanced.patcher.instructions
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.billingClientOnServiceConnectedMethod by gettingFirstMethod("Billing service connected")

internal val BytecodePatchContext.startSubscriptionActivityMethod by gettingFirstMethodDeclaratively {
    instructions(0x10008000L())
}
