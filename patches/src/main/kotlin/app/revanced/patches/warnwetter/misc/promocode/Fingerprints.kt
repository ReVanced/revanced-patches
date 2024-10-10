package app.revanced.patches.warnwetter.misc.promocode

import app.revanced.patcher.fingerprint

internal val promoCodeUnlockFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("PromoTokenVerification;") && method.name == "isValid"
    }
}
