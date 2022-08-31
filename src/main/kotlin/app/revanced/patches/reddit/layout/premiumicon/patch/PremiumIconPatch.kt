package app.revanced.patches.reddit.layout.premiumicon.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.reddit.layout.premiumicon.annotations.PremiumIconCompatibility
import app.revanced.patches.reddit.layout.premiumicon.fingerprints.PremiumIconFingerprint

@Patch
@Name("premium-icon-reddit")
@Description("Unlocking Premium Icons in reddit app.")
@PremiumIconCompatibility
@Version("0.0.1")
class PremiumIconPatch : BytecodePatch(
    listOf(
        PremiumIconFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val method = PremiumIconFingerprint.result!!.mutableMethod
        method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
        return PatchResultSuccess()
    }
}
