package app.revanced.patches.reddit.customclients.infinityforreddit.subscription

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.util.literal

internal val billingClientOnServiceConnectedFingerprint = methodFingerprint {
    strings("Billing service connected")
}

internal val startSubscriptionActivityFingerprint = methodFingerprint {
    literal {
        // Intent start flag only used in the subscription activity
        0x10008000
    }
}
