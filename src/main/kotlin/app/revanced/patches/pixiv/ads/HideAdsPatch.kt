package app.revanced.patches.pixiv.ads

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.pixiv.ads.fingerprints.IsNotPremiumFingerprint
import app.revanced.patches.pixiv.ads.fingerprints.ShouldShowAdsFingerprint

@Patch(
    name = "Hide ads",
    compatiblePackages = [CompatiblePackage("jp.pxv.android")]
)
@Suppress("unused")
object HideAdsPatch : BytecodePatch(setOf(IsNotPremiumFingerprint, ShouldShowAdsFingerprint)) {
    // Always return false in the "isNotPremium" or "shouldShowAds" method which normally returns !this.accountManager.isPremium.
    // However, this is not the method that controls the user's premium status.
    // Instead, this method is used to determine whether ads should be shown.
    override fun execute(context: BytecodeContext) {
        val method = IsNotPremiumFingerprint.result?.mutableClass?.virtualMethods?.first()
            ?: ShouldShowAdsFingerprint.result?.mutableMethod
            ?: throw ShouldShowAdsFingerprint.exception

        method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """
        )
    }
}
