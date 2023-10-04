package app.revanced.patches.vsco.misc.pro

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.vsco.misc.pro.fingerprints.RevCatSubscriptionFingerprint

@Patch(
    name = "Unlock pro",
    description = "Unlocks pro features.",
    compatiblePackages = [CompatiblePackage("com.vsco.cam")]
)
object UnlockProPatch : BytecodePatch(
    setOf(RevCatSubscriptionFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        RevCatSubscriptionFingerprint.result?.mutableMethod?.apply {
            // Set isSubscribed to true.
            addInstruction(
                0,
                """
                    const p1, 0x1
                """
            )
        } ?: throw RevCatSubscriptionFingerprint.exception
    }
}
