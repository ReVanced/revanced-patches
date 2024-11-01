package app.revanced.patches.myfitnesspal.ads

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.myfitnesspal.ads.fingerprints.IsPremiumUseCaseImplFingerprint
import app.revanced.patches.myfitnesspal.ads.fingerprints.MainActivityNavigateToNativePremiumUpsellFingerprint
import app.revanced.util.exception

@Patch(
    name = "Hide ads",
    description = "Hides most of the ads across the app.",
    compatiblePackages = [CompatiblePackage("com.myfitnesspal.android")]
)
@Suppress("unused")
object HideAdsPatch : BytecodePatch(
    setOf(IsPremiumUseCaseImplFingerprint, MainActivityNavigateToNativePremiumUpsellFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        // Overwrite the premium status specifically for ads.
        IsPremiumUseCaseImplFingerprint.result?.mutableMethod?.replaceInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """
        ) ?: throw IsPremiumUseCaseImplFingerprint.exception

        // Prevent the premium upsell dialog from showing when the main activity is launched.
        // In other places that are premium-only the dialog will still show.
        MainActivityNavigateToNativePremiumUpsellFingerprint.result?.mutableMethod?.replaceInstructions(
            0,
            "return-void"
        ) ?: throw MainActivityNavigateToNativePremiumUpsellFingerprint.exception
    }
}
