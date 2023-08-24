package app.revanced.patches.vsco.misc.pro.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.*
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.vsco.misc.pro.fingerprints.RevCatSubscriptionFingerprint


@Patch
@Name("Unlock pro")
@Description("Unlocks pro features.")
@Compatibility([Package("com.vsco.cam")])
class UnlockProPatch : BytecodePatch(
    listOf(RevCatSubscriptionFingerprint)
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
