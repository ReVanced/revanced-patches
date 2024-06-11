package app.revanced.patches.windyapp.misc.unlockpro

import app.revanced.patcher.fingerprint.methodFingerprint

internal val checkProFingerprint = methodFingerprint {
    returns("I")
    custom { methodDef, classDef ->
        classDef.endsWith("RawUserData;") && methodDef.name == "isPro"
    }
}
