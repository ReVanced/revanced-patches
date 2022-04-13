package app.revanced.patches.layout

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchMetadata
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.smali.asInstructions

class MinimizedPlayback : Patch(
    PatchMetadata(
        "minimized-playback",
        "TODO",
        "TODO"
    )
) {
    override fun execute(cache: Cache): PatchResult {
        // Instead of removing all instructions like Vanced,
        // we return the method at the beginning instead
        cache.methodMap["minimized-playback-manager"]
            .method
            .implementation!!
            .addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent().asInstructions()
            )
        return PatchResultSuccess()
    }
}