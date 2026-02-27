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
            "19.43.41",
            "20.14.43",
            "20.21.37",
            "20.31.40",
        )
    ),
    hookStringFeatureFlag = true,
    preferenceScreen = PreferenceScreen.MISC,
)
