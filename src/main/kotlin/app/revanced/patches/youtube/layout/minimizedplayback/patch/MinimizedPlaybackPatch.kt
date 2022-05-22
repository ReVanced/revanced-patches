package app.revanced.patches.youtube.layout.minimizedplayback.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.util.smali.toInstructions
import app.revanced.patches.youtube.layout.minimizedplayback.annotations.MinimizedPlaybackCompatibility
import app.revanced.patches.youtube.layout.minimizedplayback.signatures.MinimizedPlaybackManagerSignature


@Patch
@Name("minimized-playback")
@Description("Enable minimized and background playback.")
@MinimizedPlaybackCompatibility
@Version("0.0.1")

class MinimizedPlaybackPatch : BytecodePatch(
    listOf(
        MinimizedPlaybackManagerSignature
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        // Instead of removing all instructions like Vanced,
        // we return the method at the beginning instead
        signatures.first().result!!.method.implementation!!.addInstructions(
            0, """
                const/4 v0, 0x1
                return v0
                """.trimIndent().toInstructions()
        )
        return PatchResultSuccess()
    }
}