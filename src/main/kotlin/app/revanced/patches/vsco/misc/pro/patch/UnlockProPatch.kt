package app.revanced.patches.vsco.misc.pro.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.*
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.vsco.misc.pro.fingerprints.RevCatSubscriptionFingerprint


@Patch
@Name("unlock-pro")
@Description("Unlocks pro features.")
@Compatibility([Package("com.vsco.cam")])
@Version("0.0.1")
class UnlockProPatch : BytecodePatch(
    listOf(RevCatSubscriptionFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        RevCatSubscriptionFingerprint.result?.mutableMethod?.apply {
            // Set isSubscribed to true.
            addInstruction(
                0,
                """
                    const p1, 0x1
                """
            )
        } ?: return RevCatSubscriptionFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}
