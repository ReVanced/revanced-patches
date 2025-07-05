package app.revanced.patches.lightroom.misc.premium

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
) {
    compatibleWith("com.adobe.lrmobile"("9.3.0"))

    execute {
        // Set hasPremium = true.
        hasPurchasedFingerprint.method.replaceInstruction(2, "const/4 v2, 0x1")
    }
}
