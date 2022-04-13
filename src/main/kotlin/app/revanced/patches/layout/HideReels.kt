package app.revanced.patches.layout

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchMetadata
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.smali.asInstruction

class HideReels : Patch(
    PatchMetadata(
        "hide-reels",
        "TODO",
        "TODO"
    )
) {
    override fun execute(cache: Cache): PatchResult {
        val map = cache.methodMap["hide-reel-patch"]
        val implementation = map.method.implementation!!

        // HideReel will hide the reel view before it is being used,
        // so we pass the view to the HideReel method
        implementation.addInstruction(
            map.scanData.endIndex - 1,
            "invoke-static { v2 }, Lfi/razerman/youtube/XAdRemover;->HideReel(Landroid/view/View;)V".asInstruction()
        )

        return PatchResultSuccess()
    }
}