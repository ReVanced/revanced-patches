package app.revanced.patches.lightroom.misc.premium.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.lightroom.misc.premium.annotations.UnlockPremiumCompatibility
import app.revanced.patches.lightroom.misc.premium.fingerprint.HasPurchasedFingerprint

@Patch
@Name("Unlock premium")
@Description("Unlocks premium features.")
@UnlockPremiumCompatibility
class UnlockPremiumPatch : BytecodePatch(listOf(HasPurchasedFingerprint)) {
    override fun execute(context: BytecodeContext) {
         // Set hasPremium = true.
        HasPurchasedFingerprint.result?.mutableMethod?.replaceInstruction(2, "const/4 v2, 0x1")
            ?: throw HasPurchasedFingerprint.toErrorResult()
    }
}