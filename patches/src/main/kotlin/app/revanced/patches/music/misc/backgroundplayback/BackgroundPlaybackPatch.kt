package app.revanced.patches.music.misc.backgroundplayback

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.util.returnEarly

val backgroundPlaybackPatch = bytecodePatch(
    name = "Remove background playback restrictions",
    description = "Removes restrictions on background playback, including playing kids videos in the background.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch
    )

    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52",
            "8.10.52"
        )
    )

    execute {
        kidsBackgroundPlaybackPolicyControllerFingerprint.method.addInstruction(
            0,
            "return-void",
        )

        backgroundPlaybackDisableFingerprint.method.returnEarly(true)
    }
}
