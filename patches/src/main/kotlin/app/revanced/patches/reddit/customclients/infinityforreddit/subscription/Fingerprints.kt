package app.revanced.patches.reddit.customclients.infinityforreddit.subscription

import app.revanced.patcher.fingerprint
import app.revanced.util.literal

internal val billingClientOnServiceConnectedFingerprint by fingerprint {
    strings("Billing service connected")
}

internal val startSubscriptionActivityFingerprint by fingerprint {
    literal {
        // Intent start flag only used in the subscription activity
        0x10008000
    }
}
