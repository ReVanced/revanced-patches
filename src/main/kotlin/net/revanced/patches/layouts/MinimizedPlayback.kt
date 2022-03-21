package net.revanced.patches.layouts

import net.revanced.patcher.cache.Cache
import net.revanced.patcher.patch.Patch
import net.revanced.patcher.patch.PatchResult
import net.revanced.patcher.patch.PatchResultSuccess

class MinimizedPlayback: Patch("minimized-playback") {
    override fun execute(cache: Cache): PatchResult {
        cache.methods["minimized-playback-manager"].method.instructions.clear()
        return PatchResultSuccess()
    }
}