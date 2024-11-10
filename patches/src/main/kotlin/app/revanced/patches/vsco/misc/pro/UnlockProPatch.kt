package app.revanced.patches.vsco.misc.pro

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock pro",
    description = "Unlocks pro features.",
) {
    compatibleWith("com.vsco.cam"("345"))

    execute {
        // Set isSubscribed to true.
        revCatSubscriptionFingerprint.method().addInstruction(0, "const p1, 0x1")
    }
}
