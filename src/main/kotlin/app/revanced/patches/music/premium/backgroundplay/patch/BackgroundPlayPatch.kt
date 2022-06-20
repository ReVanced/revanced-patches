package app.revanced.patches.music.premium.backgroundplay.patch

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
import app.revanced.patches.music.premium.backgroundplay.annotations.BackgroundPlayCompatibility
import app.revanced.patches.music.premium.backgroundplay.signatures.BackgroundPlaybackDisableSignature

@Patch
@Name("background-play")
@Description("Enable playing music in the background.")
@BackgroundPlayCompatibility
@Version("0.0.1")
class BackgroundPlayPatch : BytecodePatch(
    listOf(
        BackgroundPlaybackDisableSignature
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        BackgroundPlaybackDisableSignature.result!!.method.implementation!!.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """.trimIndent().toInstructions()
        )

        return PatchResultSuccess()
    }
}