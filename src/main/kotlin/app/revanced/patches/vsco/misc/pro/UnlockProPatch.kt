package app.revanced.patches.vsco.misc.pro

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock pro",
    description = "Unlocks pro features.",
) {
    compatibleWith("com.vsco.cam"("345"))

    val revCatSubscriptionMatch by revCatSubscriptionFingerprint()

    execute {
        // Set isSubscribed to true.
        revCatSubscriptionMatch.mutableMethod.addInstruction(0, "const p1, 0x1")
    }
}
