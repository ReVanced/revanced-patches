package app.revanced.patches.warnwetter.misc.promocode.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.warnwetter.misc.firebasegetcert.patch.FirebaseGetCertPatch
import app.revanced.patches.warnwetter.misc.promocode.annotations.PromoCodeUnlockCompatibility
import app.revanced.patches.warnwetter.misc.promocode.fingerprints.PromoCodeUnlockFingerprint

@DependsOn(
    [
        FirebaseGetCertPatch::class
    ]
)
@Patch
@Name("Promo code unlock")
@Description("Disables the validation of promo code. Any code will work to unlock all features.")
@PromoCodeUnlockCompatibility
class PromoCodeUnlockPatch : BytecodePatch(
    listOf(
        PromoCodeUnlockFingerprint
    )
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