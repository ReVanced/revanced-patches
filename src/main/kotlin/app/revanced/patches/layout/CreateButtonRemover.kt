package app.revanced.patches.layout

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchMetadata
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.smali.asInstruction

class CreateButtonRemover : Patch(
    PatchMetadata(
        "create-button-remover",
        "TODO",
        "TODO"
    )
) {
    override fun execute(cache: Cache): PatchResult {
        val map = cache.methodMap["create-button-patch"]

        // Hide the button view via proxy by passing it to the hideCreateButton method
        map.method.implementation!!.addInstruction(
            map.scanData.endIndex,
            "invoke-static { v2 }, Lfi/razerman/youtube/XAdRemover;->hideCreateButton(Landroid/view/View;)V".asInstruction()
        )

        return PatchResultSuccess()
    }
}