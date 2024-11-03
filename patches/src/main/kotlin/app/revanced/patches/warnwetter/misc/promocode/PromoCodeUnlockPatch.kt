package app.revanced.patches.warnwetter.misc.promocode

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.warnwetter.misc.firebasegetcert.firebaseGetCertPatch
import app.revanced.util.matchOrThrow

@Suppress("unused")
val promoCodeUnlockPatch = bytecodePatch(
    name = "Promo code unlock",
    description = "Disables the validation of promo code. Any code will work to unlock all features.",
) {
    dependsOn(firebaseGetCertPatch)

    compatibleWith("de.dwd.warnapp"("4.2.2"))

    execute {
        promoCodeUnlockFingerprint.matchOrThrow.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
