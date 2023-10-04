package app.revanced.patches.warnwetter.misc.promocode

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.warnwetter.misc.firebasegetcert.FirebaseGetCertPatch
import app.revanced.patches.warnwetter.misc.promocode.fingerprints.PromoCodeUnlockFingerprint

@Patch(
    name = "Promo code unlock",
    description = "Disables the validation of promo code. Any code will work to unlock all features.",
    dependencies = [FirebaseGetCertPatch::class],
    compatiblePackages = [CompatiblePackage("de.dwd.warnapp")]
)
@Suppress("unused")
object PromoCodeUnlockPatch : BytecodePatch(
    setOf(PromoCodeUnlockFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        val method = PromoCodeUnlockFingerprint.result!!.mutableMethod
        method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
    }
}