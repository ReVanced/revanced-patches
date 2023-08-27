package app.revanced.patches.reddit.layout.premiumicon.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.reddit.layout.premiumicon.annotations.PremiumIconCompatibility
import app.revanced.patches.reddit.layout.premiumicon.fingerprints.PremiumIconFingerprint

@Patch
@Name("Premium icon reddit")
@Description("Unlocks premium Reddit app icons.")
@PremiumIconCompatibility
class PremiumIconPatch : BytecodePatch(
    listOf(
        PremiumIconFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        val method = PremiumIconFingerprint.result!!.mutableMethod
        method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
    }
}
