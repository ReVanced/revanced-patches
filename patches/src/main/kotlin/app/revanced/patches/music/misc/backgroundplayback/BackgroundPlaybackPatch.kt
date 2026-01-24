package app.revanced.patches.music.misc.backgroundplayback

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Remove background playback restrictions` by creatingBytecodePatch(
    description = "Removes restrictions on background playback, including playing kids videos in the background.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
    )

    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52",
            "8.10.52",
        ),
    )

    apply {
        kidsBackgroundPlaybackPolicyControllerMethod.returnEarly()
        backgroundPlaybackDisableMethod.returnEarly(true)
    }
}
