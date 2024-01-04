package app.revanced.patches.myfitnesspal

import app.revanced.patches.myfitnesspal.fingerprints.MainActivity_navigateToNativePremiumUpsellFingerprint
import app.revanced.patches.myfitnesspal.fingerprints.IsPremiumUseCaseImplFingerprint

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions;

@Patch(
        name = "Remove ads",
        description = "Hides most of the ads across the app.",
        compatiblePackages = [CompatiblePackage("com.myfitnesspal.android")]
)
object AdsPatch : BytecodePatch(setOf(
    IsPremiumUseCaseImplFingerprint,
    MainActivity_navigateToNativePremiumUpsellFingerprint
)) {
    override fun execute(context: BytecodeContext) {

        // patch ads lib to "return true"
        IsPremiumUseCaseImplFingerprint.result?.mutableMethod?.apply {
            replaceInstructions(
                0,
                """
                    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                    return-object v0
                """
            )
        } ?: throw PatchException("IsPremiumUseCaseImpl fingerprint not found")

        // stop MainActivity ever throwing the premium upsell dialog
        MainActivity_navigateToNativePremiumUpsellFingerprint.result?.mutableMethod?.apply {
            replaceInstructions(
                0,
                """
                    return-void
                """
            )
        } ?: throw PatchException("MainActivity fingerprint not found")
    }
}
