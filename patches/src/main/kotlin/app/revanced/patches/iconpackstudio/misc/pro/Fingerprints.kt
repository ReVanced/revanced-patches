package app.revanced.patches.iconpackstudio.misc.pro

import app.revanced.patcher.fingerprint

internal val checkProFingerprint = fingerprint {
    returns("Z")
    custom { _, classDef -> classDef.endsWith("IPSPurchaseRepository;") }
}