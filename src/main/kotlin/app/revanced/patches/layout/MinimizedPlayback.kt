package app.revanced.patches.layout

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess

class MinimizedPlayback : Patch("minimized-playback") {
    override fun execute(cache: Cache): PatchResult {
        cache.methods["minimized-playback-manager"].method.instructions.clear()
        return PatchResultSuccess()
    }
}