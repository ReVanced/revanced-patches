package app.revanced.patches.music.premium.backgroundplay.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
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
    override fun execute(data: BytecodeData): PatchResult {
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