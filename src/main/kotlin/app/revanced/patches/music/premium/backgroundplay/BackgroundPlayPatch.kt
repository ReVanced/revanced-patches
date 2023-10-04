package app.revanced.patches.music.premium.backgroundplay

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.premium.backgroundplay.fingerprints.BackgroundPlaybackDisableFingerprint


@Patch(
    name = "Background play",
    description = "Enables playing music in the background.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object BackgroundPlayPatch : BytecodePatch(setOf(BackgroundPlaybackDisableFingerprint)) {
    override fun execute(context: BytecodeContext) =
        BackgroundPlaybackDisableFingerprint.result?.mutableMethod?.addInstructions(
            0, """
                const/4 v0, 0x1
                return v0
            """
        ) ?: throw BackgroundPlaybackDisableFingerprint.exception
}