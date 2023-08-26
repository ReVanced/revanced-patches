package app.revanced.patches.inshorts.ad.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.inshorts.ad.annotations.HideAdsCompatibility
import app.revanced.patches.inshorts.ad.fingerprints.InshortsAdsFingerprint

@Patch
@Name("Hide ads")
@Description("Removes ads from Inshorts.")
@HideAdsCompatibility
class HideAdsPatch : BytecodePatch(
    listOf(InshortsAdsFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        InshortsAdsFingerprint.result?.let { result ->
            result.apply {
                mutableMethod.addInstruction(
                    0,
                    """
                        return-void
                    """
                )
            }
        } ?: throw InshortsAdsFingerprint.exception
    }
}
