package app.revanced.patches.candylinkvpn.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.candylinkvpn.annotations.UnlockProCompatibility
import app.revanced.patches.candylinkvpn.fingerprints.IsPremiumPurchasedFingerprint

@Patch
@Name("Unlock pro")
@Description("Unlocks premium features.")
@UnlockProCompatibility
class UnlockProPatch : BytecodePatch(
    listOf(IsPremiumPurchasedFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        IsPremiumPurchasedFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
               const/4 v0, 0x1
               return v0
            """
        ) ?: throw IsPremiumPurchasedFingerprint.exception
    }
}