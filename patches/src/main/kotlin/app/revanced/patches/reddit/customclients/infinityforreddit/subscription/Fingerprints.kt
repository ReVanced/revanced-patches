package app.revanced.patches.reddit.customclients.infinityforreddit.subscription

import app.revanced.patcher.gettingFirstMutableMethod
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.invoke
import app.revanced.patcher.instructions
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.billingClientOnServiceConnectedMethod by gettingFirstMutableMethod("Billing service connected")

internal val BytecodePatchContext.startSubscriptionActivityMethod by gettingFirstMutableMethodDeclaratively {
    instructions(0x10008000L())
}
