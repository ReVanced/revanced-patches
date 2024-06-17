package app.revanced.patches.lightroom.misc.premium

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
) {
    compatibleWith("com.adobe.lrmobile")

    val hasPurchasedMatch by hasPurchasedFingerprint()

    execute {
        // Set hasPremium = true.
        hasPurchasedMatch.mutableMethod.replaceInstruction(2, "const/4 v2, 0x1")
    }
}
