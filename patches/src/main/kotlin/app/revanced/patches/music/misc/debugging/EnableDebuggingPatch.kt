package app.revanced.patches.music.misc.debugging

import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.music.playservice.is_8_40_or_greater
import app.revanced.patches.music.playservice.is_8_41_or_greater
import app.revanced.patches.shared.misc.debugging.enableDebuggingPatch

@Suppress("unused")
val enableDebuggingPatch = enableDebuggingPatch(
    block = {
        dependsOn(
            sharedExtensionPatch,
            settingsPatch,
        )

        compatibleWith(
            "com.google.android.apps.youtube.music"(
                "7.29.52",
                "8.10.52",
                "8.37.56",
                "8.40.54",
            )
        )
    },
    // String feature flag does not appear to be present with YT Music.
    hookStringFeatureFlag = { false },
    // 8.40 has changes not worth supporting.
    hookLongFeatureFlag = { !is_8_40_or_greater || is_8_41_or_greater },
    hookDoubleFeatureFlag = { !is_8_40_or_greater || is_8_41_or_greater },
    preferenceScreen = PreferenceScreen.MISC,
)
