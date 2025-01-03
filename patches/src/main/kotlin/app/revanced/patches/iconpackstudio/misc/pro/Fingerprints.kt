package app.revanced.patches.iconpackstudio.misc.pro

import app.revanced.patcher.fingerprint

internal val checkProFingerprint by fingerprint {
    returns("Z")
    custom { _, classDef -> classDef.endsWith("IPSPurchaseRepository;") }
}