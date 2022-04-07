package app.revanced.patches.layout

import app.revanced.patcher.cache.Cache
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x

class MinimizedPlayback : Patch("minimized-playback") {
    override fun execute(cache: Cache): PatchResult {
        // Instead of removing all instructions like Vanced,
        // we return the method at the beginning instead
        cache.methodMap["minimized-playback-manager"]
            .resolveAndGetMethod()
            .implementation!!
            .addInstruction(
                0,
                BuilderInstruction10x(Opcode.RETURN_VOID)
            )
        return PatchResultSuccess()
    }
}