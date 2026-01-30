package app.revanced.patches.music.misc.debugging

import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.shared.misc.debugging.enableDebuggingPatch

@Suppress("unused")
val enableDebuggingPatch = enableDebuggingPatch(
    sharedExtensionPatch = sharedExtensionPatch,
    settingsPatch = settingsPatch,
    compatibleWithPackages = arrayOf(
        "com.google.android.apps.youtube.music" to setOf(
            "7.29.52",
            "8.10.52"
        )
    ),
    // String feature flag does not appear to be present with YT Music.
    hookStringFeatureFlag = false,
    preferenceScreen = PreferenceScreen.MISC,
)
