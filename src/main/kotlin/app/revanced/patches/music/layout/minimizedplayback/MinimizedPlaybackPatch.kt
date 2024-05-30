package app.revanced.patches.music.layout.minimizedplayback

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.music.layout.minimizedplayback.fingerprints.backgroundPlaybackDisableFingerprint
import app.revanced.patches.music.layout.minimizedplayback.fingerprints.kidsMiniPlaybackPolicyControllerFingerprint

@Suppress("unused")
val minimizedPlaybackPatch = bytecodePatch(
    name = "Minimized playback",
    description = "Unlocks options for picture-in-picture and background playback.",
) {
    compatibleWith("com.google.android.apps.youtube.music")

    val kidsMinimizedPlaybackPolicyControllerResult by kidsMiniPlaybackPolicyControllerFingerprint
    val backgroundPlaybackDisableResult by backgroundPlaybackDisableFingerprint

    execute {
        kidsMinimizedPlaybackPolicyControllerResult.mutableMethod.addInstruction(
            0,
            "return-void",
        )

        backgroundPlaybackDisableResult.mutableMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
