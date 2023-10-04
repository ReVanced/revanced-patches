package app.revanced.patches.reddit.layout.premiumicon

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.layout.premiumicon.fingerprints.HasPremiumIconAccessFingerprint


@Patch(
    name = "Unlock premium Reddit icons",
    compatiblePackages = [CompatiblePackage("com.reddit.frontpage")]
)
@Suppress("unused")
object UnlockPremiumIconPatch : BytecodePatch(setOf(HasPremiumIconAccessFingerprint)) {
    override fun execute(context: BytecodeContext) {
        HasPremiumIconAccessFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        ) ?: throw HasPremiumIconAccessFingerprint.exception
    }
}
