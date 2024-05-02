package app.revanced.patches.warnwetter.misc.promocode.fingerprints
import app.revanced.patcher.fingerprint.methodFingerprint

internal val promoCodeUnlockFingerprint = methodFingerprint {
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("PromoTokenVerification;") && methodDef.name == "isValid"
    }
}
