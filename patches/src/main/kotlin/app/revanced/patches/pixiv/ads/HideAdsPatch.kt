package app.revanced.patches.pixiv.ads

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
) {
    compatibleWith("jp.pxv.android")

    val isNotPremiumMatch by isNotPremiumFingerprint()

    // Always return false in the "isNotPremium" method which normally returns !this.accountManager.isPremium.
    // However, this is not the method that controls the user's premium status.
    // Instead, this method is used to determine whether ads should be shown.
    execute {
        isNotPremiumMatch.mutableMethod.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
