package app.revanced.patches.music.premium.backgroundplay.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.music.premium.backgroundplay.annotations.BackgroundPlayCompatibility
import app.revanced.patches.music.premium.backgroundplay.fingerprints.BackgroundPlaybackDisableFingerprint

@Patch
@Name("background-play")
@Description("Enables playing music in the background.")
@BackgroundPlayCompatibility
@Version("0.0.1")
class BackgroundPlayPatch : BytecodePatch(
    listOf(
        BackgroundPlaybackDisableFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        BackgroundPlaybackDisableFingerprint.result!!.mutableMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )

        return PatchResultSuccess()
    }
}