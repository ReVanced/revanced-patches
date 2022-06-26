package app.revanced.patches.youtube.layout.minimizedplayback.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.youtube.layout.minimizedplayback.annotations.MinimizedPlaybackCompatibility
import app.revanced.patches.youtube.layout.minimizedplayback.fingerprints.MinimizedPlaybackManagerFingerprint


@Patch
@Name("minimized-playback")
@Description("Enable minimized and background playback.")
@MinimizedPlaybackCompatibility
@Version("0.0.1")
class MinimizedPlaybackPatch : BytecodePatch(
    listOf(
        MinimizedPlaybackManagerFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        // Instead of removing all instructions like Vanced,
        // we return the method at the beginning instead
        MinimizedPlaybackManagerFingerprint.result!!.mutableMethod.addInstructions(
            0, """
                const/4 v0, 0x1
                return v0
                """
        )
        return PatchResultSuccess()
    }
}