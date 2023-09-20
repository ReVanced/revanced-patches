package app.revanced.patches.memegenerator.misc.pro

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.memegenerator.detection.license.LicenseValidationPatch
import app.revanced.patches.memegenerator.detection.signature.SignatureVerificationPatch
import app.revanced.patches.memegenerator.misc.pro.fingerprints.IsFreeVersionFingerprint

@Patch(
    name = "Unlock pro",
    dependencies = [
        SignatureVerificationPatch::class,
        LicenseValidationPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.zombodroid.MemeGenerator", [
                "4.6364",
                "4.6370",
                "4.6375",
                "4.6377"
            ]
        )
    ]
)
@Suppress("unused")
object UnlockProVersionPatch : BytecodePatch(
    setOf(IsFreeVersionFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        IsFreeVersionFingerprint.result?.apply {
            mutableMethod.replaceInstructions(0,
                """
                    sget-object p0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                    return-object p0
                """
            )
        } ?: throw IsFreeVersionFingerprint.exception
    }
}