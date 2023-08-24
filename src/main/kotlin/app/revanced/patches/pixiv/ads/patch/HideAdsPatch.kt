package app.revanced.patches.pixiv.ads.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.*
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.pixiv.ads.fingerprints.IsNotPremiumFingerprint

@Patch
@Name("Hide ads")
@Description("Hides ads.")
@Compatibility([Package("jp.pxv.android")])
class HideAdsPatch : BytecodePatch(listOf(IsNotPremiumFingerprint)) {
    override fun execute(context: BytecodeContext) {
        // Always return false in the "isNotPremium" method which normally returns !this.accountManager.isPremium.
        // However, this is not the method that controls the user's premium status.
        // Instead, this method is used to determine whether ads should be shown.
        IsNotPremiumFingerprint.result?.mutableClass?.virtualMethods?.first()?.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """
        ) ?: throw IsNotPremiumFingerprint.exception
    }
}