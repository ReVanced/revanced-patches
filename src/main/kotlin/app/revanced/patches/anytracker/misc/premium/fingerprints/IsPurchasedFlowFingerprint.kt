package app.revanced.patches.anytracker.misc.premium.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object IsPurchasedFlowFingerprint : MethodFingerprint(
    "Landroidx/lifecycle/LiveData",
    strings = listOf("premium_user", "sku"),
)
