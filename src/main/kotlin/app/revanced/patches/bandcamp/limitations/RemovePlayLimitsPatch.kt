package app.revanced.patches.bandcamp.limitations

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.bandcamp.limitations.fingerprints.HandlePlaybackLimitsPatch
import app.revanced.util.exception

@Patch(
    name = "Remove play limits",
    description = "Disables purchase nagging and playback limits of not purchased tracks.",
    compatiblePackages = [CompatiblePackage("com.bandcamp.android")],
)
@Suppress("unused")
object RemovePlayLimitsPatch : BytecodePatch(
    setOf(HandlePlaybackLimitsPatch),
) {
    override fun execute(context: BytecodeContext) =
        HandlePlaybackLimitsPatch.result?.mutableMethod?.addInstructions(0, "return-void")
            ?: throw HandlePlaybackLimitsPatch.exception
}
