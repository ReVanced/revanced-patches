package app.revanced.patches.layout

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.smali.asInstruction

class HideReels : Patch("hide-reels") {
    override fun execute(cache: Cache): PatchResult {
        val implementation = cache.methodMap["hide-reel-patch"].resolveAndGetMethod().implementation!!

        // HideReel will hide the reel view before it is being used,
        // so we pass the view to the HideReel method
        implementation.addInstruction(
            22,
            "invoke-static { v2 }, Lfi/razerman/youtube/XAdRemover;->HideReel(Landroid/view/View;)V".asInstruction()
        )

        return PatchResultSuccess()
    }
}