package app.revanced.patches.memegenerator.misc.pro.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.memegenerator.detection.license.patch.LicenseValidationPatch
import app.revanced.patches.memegenerator.detection.signature.patch.SignatureVerificationPatch
import app.revanced.patches.memegenerator.misc.pro.annotations.UnlockProCompatibility
import app.revanced.patches.memegenerator.misc.pro.fingerprint.IsFreeVersionFingerprint

@Patch
@Name("Unlock pro")
@Description("Unlocks pro features.")
@DependsOn([
    SignatureVerificationPatch::class,
    LicenseValidationPatch::class
])
@UnlockProCompatibility
class UnlockProVersionPatch : BytecodePatch(
    listOf(
        IsFreeVersionFingerprint
    )
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