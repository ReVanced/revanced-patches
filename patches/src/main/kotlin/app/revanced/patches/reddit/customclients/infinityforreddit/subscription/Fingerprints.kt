package app.revanced.patches.reddit.customclients.infinityforreddit.subscription

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.invoke
import app.revanced.patcher.instructions
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.billingClientOnServiceConnectedMethod by gettingFirstMethodDeclaratively {
    instructions("Billing service connected"(String::contains))
}

internal val BytecodePatchContext.startSubscriptionActivityMethod by gettingFirstMethodDeclaratively {
    instructions(0x10008000L())
}
