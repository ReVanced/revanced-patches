package app.revanced.patches.reddit.customclients.infinityforreddit.subscription

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.literal
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.billingClientOnServiceConnectedMethod by gettingFirstMutableMethodDeclaratively("Billing service connected")

internal val BytecodePatchContext.startSubscriptionActivityMethod by gettingFirstMutableMethodDeclaratively {
    instructions(
        literal(0x10008000)
    )
}
