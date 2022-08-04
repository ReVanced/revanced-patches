package app.revanced.patches.warnwetter.misc.promocode.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patches.warnwetter.misc.promocode.annotations.PromoCodeUnlockCompatibility

@Name("promo-code-unlock-fingerprint")
@MatchingMethod(
    "Lde/dwd/warnapp/model/PromoTokenVerification;", "isValid"
)
@PromoCodeUnlockCompatibility
@Version("0.0.1")
object PromoCodeUnlockFingerprint : MethodFingerprint(
    null,
    null,
    null,
    null,
    null,
    { methodDef ->
        methodDef.definingClass.endsWith("PromoTokenVerification;") && methodDef.name == "isValid"
    }
)