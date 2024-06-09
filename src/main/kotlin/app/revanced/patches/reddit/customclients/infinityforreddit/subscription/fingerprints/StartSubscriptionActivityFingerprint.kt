package app.revanced.patches.reddit.customclients.infinityforreddit.subscription.fingerprints

import app.revanced.util.patch.literalValueFingerprint

internal val startSubscriptionActivityFingerprint = literalValueFingerprint(
    literalSupplier = {
        // Intent start flag only used in the subscription activity
        0x10008000
    },
)
