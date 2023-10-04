package app.revanced.patches.pixiv.ads

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.pixiv.ads.fingerprints.IsNotPremiumFingerprint

@Patch(
    name = "Hide ads",
    compatiblePackages = [CompatiblePackage("jp.pxv.android")]
)
@Suppress("unused")
object HideAdsPatch : BytecodePatch(setOf(IsNotPremiumFingerprint)) {
    // Always return false in the "isNotPremium" method which normally returns !this.accountManager.isPremium.
    // However, this is not the method that controls the user's premium status.
    // Instead, this method is used to determine whether ads should be shown.
    override fun execute(context: BytecodeContext) =
        IsNotPremiumFingerprint.result?.mutableClass?.virtualMethods?.first()?.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """
        ) ?: throw IsNotPremiumFingerprint.exception
}