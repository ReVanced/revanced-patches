package app.revanced.patches.windyapp.misc.unlockpro

import app.revanced.patcher.fingerprint

internal val checkProFingerprint = fingerprint {
    returns("I")
    custom { method, classDef ->
        classDef.endsWith("RawUserData;") && method.name == "isPro"
    }
}
