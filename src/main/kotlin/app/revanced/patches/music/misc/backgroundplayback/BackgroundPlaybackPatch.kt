package app.revanced.patches.music.misc.backgroundplayback

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.misc.backgroundplayback.fingerprints.BackgroundPlaybackDisableFingerprint
import app.revanced.patches.music.misc.backgroundplayback.fingerprints.KidsBackgroundPlaybackPolicyControllerFingerprint
import app.revanced.util.resultOrThrow

@Patch(
    name = "Remove background playback restrictions",
    description = "Removes restrictions on background playback, including playing kids videos in the background.",
    compatiblePackages = [
        CompatiblePackage("com.google.android.apps.youtube.music")
    ]
)
@Suppress("unused")
object BackgroundPlaybackPatch : BytecodePatch(
    setOf(
        KidsBackgroundPlaybackPolicyControllerFingerprint,
        BackgroundPlaybackDisableFingerprint,
    ),
) {
    override fun execute(context: BytecodeContext) {
        KidsBackgroundPlaybackPolicyControllerFingerprint.resultOrThrow().mutableMethod.addInstruction(
            0,
            "return-void",
        )

        BackgroundPlaybackDisableFingerprint.resultOrThrow().mutableMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
