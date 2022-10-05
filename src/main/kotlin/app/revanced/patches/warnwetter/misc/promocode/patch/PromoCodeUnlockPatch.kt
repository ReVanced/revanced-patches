package app.revanced.patches.warnwetter.misc.promocode.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
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
@Name("promo-code-unlock")
@Description("Disables the validation of promo code. Any code will work to unlock all features.")
@PromoCodeUnlockCompatibility
@Version("0.0.1")
class PromoCodeUnlockPatch : BytecodePatch(
    listOf(
        PromoCodeUnlockFingerprint
    )
) {

    override fun execute(context: BytecodeContext): PatchResult {
        val method = PromoCodeUnlockFingerprint.result!!.mutableMethod
        method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )

        return PatchResultSuccess()
    }


}