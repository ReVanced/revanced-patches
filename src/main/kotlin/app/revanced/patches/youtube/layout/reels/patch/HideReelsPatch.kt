package app.revanced.patches.youtube.layout.reels.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.util.smali.toInstruction
import app.revanced.patches.youtube.layout.reels.annotations.HideReelsCompatibility
import app.revanced.patches.youtube.layout.reels.signatures.HideReelsSignature

//@Patch TODO: this is currently in the general-bytecode-ads patch due to the integrations having a preference for including reels or not. Move it here.
@Name("hide-reels")
@Description("Hide reels on the page.")
@HideReelsCompatibility
@Version("0.0.1")
class HideReelsPatch : BytecodePatch(
    listOf(
        HideReelsSignature
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val result = HideReelsSignature.result!!
        val implementation = result.method.implementation!!

        // HideReel will hide the reel view before it is being used,
        // so we pass the view to the HideReel method
        implementation.addInstruction(
            result.scanResult.endIndex,
            "invoke-static { v2 }, Lfi/razerman/youtube/XAdRemover;->HideReel(Landroid/view/View;)V".toInstruction()
        )

        return PatchResultSuccess()
    }
}