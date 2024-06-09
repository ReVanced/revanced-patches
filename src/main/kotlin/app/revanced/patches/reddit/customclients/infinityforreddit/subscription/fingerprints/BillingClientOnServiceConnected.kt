package app.revanced.patches.reddit.customclients.infinityforreddit.subscription.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val billingClientOnServiceConnectedFingerprint = methodFingerprint {
    strings("Billing service connected")
}