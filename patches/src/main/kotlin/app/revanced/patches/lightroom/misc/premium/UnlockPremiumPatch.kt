package app.revanced.patches.lightroom.misc.premium

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.matchOrThrow

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
) {
    compatibleWith("com.adobe.lrmobile")

    execute {
        // Set hasPremium = true.
        hasPurchasedFingerprint.matchOrThrow.method.replaceInstruction(2, "const/4 v2, 0x1")
    }
}
