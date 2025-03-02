package app.revanced.patches.vsco.misc.pro

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Deprecated("This patch is deprecated because it does not work anymore and will be removed in the future.")
@Suppress("unused")
val unlockProPatch = bytecodePatch(
    description = "Unlocks pro features.",
) {
    compatibleWith("com.vsco.cam"("345"))

    execute {
        // Set isSubscribed to true.
        revCatSubscriptionFingerprint.method.addInstruction(0, "const p1, 0x1")
    }
}
