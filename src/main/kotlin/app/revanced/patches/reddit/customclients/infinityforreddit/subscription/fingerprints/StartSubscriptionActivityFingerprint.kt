package app.revanced.patches.reddit.customclients.infinityforreddit.subscription.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.util.literal

internal val startSubscriptionActivityFingerprint = methodFingerprint {
    literal {
        // Intent start flag only used in the subscription activity
        0x10008000
    }
}
