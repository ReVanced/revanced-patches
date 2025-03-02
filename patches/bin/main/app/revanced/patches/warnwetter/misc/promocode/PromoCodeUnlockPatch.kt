package app.revanced.patches.warnwetter.misc.promocode

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.warnwetter.misc.firebasegetcert.firebaseGetCertPatch

@Suppress("unused")
val promoCodeUnlockPatch = bytecodePatch(
    name = "Promo code unlock",
    description = "Disables the validation of promo code. Any code will work to unlock all features.",
) {
    dependsOn(firebaseGetCertPatch)

    compatibleWith("de.dwd.warnapp"("4.2.2"))

    execute {
        promoCodeUnlockFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
