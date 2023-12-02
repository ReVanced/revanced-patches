package app.revanced.patches.reddit.ad.general.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object AdPostFingerprint : MethodFingerprint(
    "V",
    // "children" are present throughout multiple versions
    strings = listOf("children"),
    customFingerprint = { methodDef, _ -> methodDef.definingClass.endsWith("Listing;") },
)
