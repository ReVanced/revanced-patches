package app.revanced.patches.reddit.customclients.infinityforreddit.subscription

import app.revanced.patcher.fingerprint
import app.revanced.patcher.literal

internal val billingClientOnServiceConnectedFingerprint by fingerprint {
    strings("Billing service connected")
}

internal val startSubscriptionActivityFingerprint by fingerprint {
    instructions(
        literal(0x10008000) // Intent start flag only used in the subscription activity
    )
}
