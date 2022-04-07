package app.revanced.patches.layout

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.smali.asInstruction

class CreateButtonRemover : Patch("create-button-remover") {
    override fun execute(cache: Cache): PatchResult {
        val map = cache.methodMap["create-button-patch"]

        // Hide the button view via proxy by passing it to the hideCreateButton method
        map.resolveAndGetMethod().implementation!!.addInstruction(
            map.scanData.endIndex,
            "invoke-static { v6 }, Lfi/razerman/youtube/XAdRemover;->hideCreateButton(Landroid/view/View;)V".asInstruction()
        )

        return PatchResultSuccess()
    }
}