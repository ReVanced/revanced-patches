package app.revanced.patches.reddit.customclients.infinityforreddit.subscription

import app.revanced.patcher.fingerprint
import app.revanced.util.literal

internal val billingClientOnServiceConnectedFingerprint = fingerprint {
    strings("Billing service connected")
}

internal val startSubscriptionActivityFingerprint = fingerprint {
    literal {
        // Intent start flag only used in the subscription activity
        0x10008000
    }
}
