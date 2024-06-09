package app.revanced.patches.reddit.ad.general.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val adPostFingerprint = methodFingerprint {
    returns("V")
    // "children" are present throughout multiple versions
    strings("children")
    custom { _, classDef -> classDef.endsWith("Listing;") }
}