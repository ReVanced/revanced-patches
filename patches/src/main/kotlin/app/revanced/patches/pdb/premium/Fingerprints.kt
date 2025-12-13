package app.revanced.patches.pdb.premium

import app.revanced.patcher.fingerprint

/**
 * Fingerprint for Tweaks.isMockProUser() method.
 * This is a built-in developer testing method that bypasses Pro checks when returning true.
 */
internal val isMockProUserFingerprint = fingerprint {
    returns("Z") // boolean
    parameters() // no parameters
    custom { method, classDef ->
        classDef.type == "Lpdb/app/tweak/Tweaks;" && method.name == "isMockProUser"
    }
}
