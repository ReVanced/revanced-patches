package app.revanced.patches.warnwetter.misc.promocode

import app.revanced.patcher.fingerprint.methodFingerprint

internal val promoCodeUnlockFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("PromoTokenVerification;") && methodDef.name == "isValid"
    }
}
