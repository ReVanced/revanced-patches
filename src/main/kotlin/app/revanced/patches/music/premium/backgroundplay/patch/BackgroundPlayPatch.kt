package app.revanced.patches.music.premium.backgroundplay.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.music.annotations.MusicCompatibility
import app.revanced.patches.music.premium.backgroundplay.fingerprints.BackgroundPlaybackDisableFingerprint

@Patch
@Name("Background play")
@Description("Enables playing music in the background.")
@MusicCompatibility
class BackgroundPlayPatch : BytecodePatch(
    listOf(BackgroundPlaybackDisableFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        BackgroundPlaybackDisableFingerprint.result!!.mutableMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
    }
}