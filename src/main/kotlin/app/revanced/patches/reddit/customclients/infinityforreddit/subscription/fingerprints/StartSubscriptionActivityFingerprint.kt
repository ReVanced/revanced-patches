package app.revanced.patches.reddit.customclients.infinityforreddit.subscription.fingerprints

import app.revanced.util.patch.LiteralValueFingerprint

internal object StartSubscriptionActivityFingerprint : LiteralValueFingerprint(
    literalSupplier = { 0x10008000 } // Intent start flag only used in the subscription activity
)