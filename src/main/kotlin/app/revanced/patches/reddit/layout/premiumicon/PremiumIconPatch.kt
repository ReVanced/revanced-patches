package app.revanced.patches.reddit.layout.premiumicon

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.layout.premiumicon.fingerprints.PremiumIconFingerprint
import app.revanced.util.exception

@Patch(
    name = "Premium icon",
    description = "Unlocks premium app icons.",
    compatiblePackages = [CompatiblePackage("com.reddit.frontpage")]
)
@Suppress("unused")
object PremiumIconPatch : BytecodePatch(
    setOf(PremiumIconFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        PremiumIconFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructions(
                    0, """
                        const/4 v0, 0x1
                        return v0
                        """
                )
            }
        } ?: throw PremiumIconFingerprint.exception

    }
}
