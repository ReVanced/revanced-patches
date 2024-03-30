package app.revanced.patches.music.layout.minimizedplayback

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.layout.minimizedplayback.fingerprints.BackgroundPlaybackDisableFingerprint
import app.revanced.patches.music.layout.minimizedplayback.fingerprints.KidsMinimizedPlaybackPolicyControllerFingerprint
import app.revanced.util.exception

@Patch(
    name = "Minimized playback",
    description = "Unlocks options for picture-in-picture and background playback.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")],
)
@Suppress("unused")
object MinimizedPlaybackPatch : BytecodePatch(
    setOf(
        KidsMinimizedPlaybackPolicyControllerFingerprint,
        BackgroundPlaybackDisableFingerprint,
    ),
) {
    override fun execute(context: BytecodeContext) {
        KidsMinimizedPlaybackPolicyControllerFingerprint.result?.mutableMethod?.addInstruction(
            0,
            "return-void",
        ) ?: throw KidsMinimizedPlaybackPolicyControllerFingerprint.exception

        BackgroundPlaybackDisableFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        ) ?: throw BackgroundPlaybackDisableFingerprint.exception
    }
}
