package app.revanced.patches.music.misc.backgroundplayback

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val backgroundPlaybackPatch = bytecodePatch(
    name = "Remove background playback restrictions",
    description = "Removes restrictions on background playback.",

) {
    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "6.45.54",
            "6.51.53",
            "7.01.53",
            "7.02.52",
            "7.03.52",
        ),
    )

    val kidsBackgroundPlaybackPolicyControllerFingerprintResult by kidsBackgroundPlaybackPolicyControllerFingerprint()
    val backgroundPlaybackDisableFingerprintResult by backgroundPlaybackDisableFingerprint()

    execute {
        kidsBackgroundPlaybackPolicyControllerFingerprintResult.mutableMethod.addInstruction(
            0,
            "return-void",
        )

        backgroundPlaybackDisableFingerprintResult.mutableMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
