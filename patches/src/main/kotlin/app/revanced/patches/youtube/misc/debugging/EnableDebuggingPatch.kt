package app.revanced.patches.youtube.misc.debugging

import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch;
import app.revanced.patches.shared.misc.debugging.enableDebuggingPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

@Suppress("unused")
val enableDebuggingPatch = enableDebuggingPatch(
    sharedExtensionPatch = sharedExtensionPatch,
    settingsPatch = settingsPatch,
    compatibleWithPackages = arrayOf(
        "com.google.android.youtube" to setOf(
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45"
        )
    ),
    hookStringFeatureFlag = true,
    preferenceScreen = PreferenceScreen.MISC,
)
