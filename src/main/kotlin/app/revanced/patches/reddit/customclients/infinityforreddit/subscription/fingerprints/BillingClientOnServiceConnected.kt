package app.revanced.patches.reddit.customclients.infinityforreddit.subscription.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object BillingClientOnServiceConnected : MethodFingerprint(
    strings = listOf("Billing service connected"),
)
