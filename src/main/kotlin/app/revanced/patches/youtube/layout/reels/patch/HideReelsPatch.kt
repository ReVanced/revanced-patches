package app.revanced.patches.youtube.layout.reels.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.layout.reels.annotations.HideReelsCompatibility
import app.revanced.patches.youtube.layout.reels.fingerprints.HideReelsFingerprint

//@Patch TODO: this is currently in the general-bytecode-ads patch due to the integrations having a preference for including reels or not. Move it here.
@Name("hide-reels")
@Description("Hides reels on the page.")
@HideReelsCompatibility
@Version("0.0.1")
class HideReelsPatch : BytecodePatch(
    listOf(
        HideReelsFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val result = HideReelsFingerprint.result!!

        // HideReel will hide the reel view before it is being used,
        // so we pass the view to the HideReel method
        result.mutableMethod.addInstruction(
            result.patternScanResult!!.endIndex,
            "invoke-static { v2 }, Lapp/revanced/integrations/patches/HideReelsPatch;->HideReel(Landroid/view/View;)V"
        )

        return PatchResultSuccess()
    }
}
