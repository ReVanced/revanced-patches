package app.revanced.patches.warnwetter.misc.promocode

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.warnwetter.misc.firebasegetcert.firebaseGetCertPatch
import app.revanced.util.returnEarly

@Suppress("unused")
val `Promo code unlock` by creatingBytecodePatch(
    description = "Disables the validation of promo code. Any code will work to unlock all features.",
) {
    dependsOn(firebaseGetCertPatch)

    compatibleWith("de.dwd.warnapp"("4.2.2"))

    apply {
        promoCodeUnlockMethod.returnEarly(true)
    }
}
